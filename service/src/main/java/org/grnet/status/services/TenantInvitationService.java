package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationActionResponse;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationResponse;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationRequest;
import org.grnet.status.entities.TenantInvitation;
import org.grnet.status.enums.InvitationAction;
import org.grnet.status.enums.InvitationStatus;
import org.grnet.status.mappers.TenantInvitationMapper;
import org.grnet.status.repositories.TenantInvitationRepository;
import org.grnet.status.repositories.TenantRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.grnet.status.enums.InvitationAction.REVOKE;


@ApplicationScoped
public class TenantInvitationService {

    @Inject
    TenantInvitationRepository tenantInvitationRepository;

    @Inject
    TenantRepository tenantRepository;

    @Inject
    MailerService mailerService;
    @Inject
    GroupManagementService groupManagementService;
    @ConfigProperty(name = "api.ui.url")
    String uiBaseUrl;
    @Inject
    ManagedExecutor executor;


    @Transactional
    public TenantInvitationResponse createInvitation(String tenantId, TenantInvitationRequest request, String createdBy) {

        var existingInvitation = tenantInvitationRepository.findPendingInvitationsByTenantAndEmail(tenantId, request.email);
        var tenant = tenantRepository.findById(tenantId);

        if (existingInvitation.isPresent()) {

            var existing = existingInvitation.get();
            var invitationUrl = uiBaseUrl + "/invitation/" + existing.id;

            mailerService.sendTenantInvitationEmail(
                    List.of(existing.email),
                    tenant.name,
                    existing.role,
                    invitationUrl
            );

            return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(existing);
        }

        var invitation = TenantInvitationMapper.INSTANCE.tenantInvitationToEntity(request);

        invitation.tenant = tenant;
        invitation.status = InvitationStatus.PENDING;
        invitation.createdBy = createdBy;

        tenantInvitationRepository.persist(invitation);

        var invitationUrl = uiBaseUrl + "/invitation/" + invitation.id;

        mailerService.sendTenantInvitationEmail(
                List.of(invitation.email),
                tenant.name,
                invitation.role,
                invitationUrl
        );

        return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(invitation);
    }

    /**
     * Returns all invitations for the authenticated user.
     */
    public PageResource<TenantInvitationResponse> getAllInvitationsByUser(String userEmail, int page, int size, UriInfo uriInfo) {

        var invitations = tenantInvitationRepository.findAllByEmail(userEmail, page , size);

        return new PageResource<>(invitations, TenantInvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }

    public TenantInvitationResponse getInvitationById(String id, String userEmail) {

        var invitations = tenantInvitationRepository.findById(id);

        enforceInviteOwnership(invitations.email, userEmail);

        return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(invitations);
    }

    public PageResource<TenantInvitationResponse> getInvitationsByPageAndSize (String search, String sort, String order, int page, int size, UriInfo uriInfo) {

        var invitations = tenantInvitationRepository.fetchInvitationsByPageAndSize(search, sort, order, page, size);

        return new PageResource<>(invitations, TenantInvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }



    public PageResource<TenantInvitationResponse> getInvitationsByTenantByPageAndSize (String search, String sort, String order, String tenantId, int page, int size, UriInfo uriInfo) {

        var tenantInvitations = tenantInvitationRepository.fetchInvitationsByTenantByPageAndSize(search, sort, order, tenantId, page, size);

        return new PageResource<>(tenantInvitations, TenantInvitationMapper.INSTANCE.listToDtos(tenantInvitations.list()), uriInfo);
    }

    @Transactional
    public TenantInvitationResponse respondToInvitation(String invitationId,
                                                        TenantInvitationActionResponse request,
                                                        String userEmail,
                                                        String userUniqueId,
                                                        String username) {

        var invitation = tenantInvitationRepository.findById(invitationId);

        enforceInviteOwnership(invitation.email, userEmail);
        enforcePending(invitation.status);

        if (request.action == InvitationAction.ACCEPT) {
            try {
                Log.info("Adding user to tenant group.");
                groupManagementService.addUserToTenantGroup(invitation.tenant.name, username, invitation.role);
            } catch (Exception e) {
                Log.warn("Failed to add user to tenant group.", e);
                // invitation stays PENDING
                throw new WebApplicationException(
                        "The user could not be added to the tenant group at the moment.", 503
                );
            }
        }

        var result = respond(invitation, request, userEmail, userUniqueId);

        executor.runAsync(() -> {
            try {
                Log.info("Sending invitation notifications.");
                sendInvitationNotifications(result);
            } catch (Exception e) {
                Log.warn("Invitation notifications failed (async).", e);
            }
        });

        return result;
    }

    public TenantInvitationResponse revokeInvitation(String tenantId, String invitationId, String userUniqueId) {

        return revoke(tenantId, invitationId, userUniqueId);
    }


    /**
     * Accept or reject invitation for the authenticated user.
     * On ACCEPT we add user to the tenant role group and send confirmation emails.
     */
    @Transactional
    public TenantInvitationResponse respond(TenantInvitation invitation,
                                            TenantInvitationActionResponse request,
                                            String userEmail,
                                            String userUniqueId) {

        var newStatus = mapToStatus(request.action);

        invitation.status = newStatus;
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(invitation);
    }

    @Transactional
    public TenantInvitationResponse revoke(String tenantId, String invitationId, String userUniqueId) {

        var invitation = tenantInvitationRepository.findById(invitationId);

        if (!Objects.equals(invitation.tenant.getId(), tenantId)) {
            throw new WebApplicationException("Invitation is not linked to this tenant.", 409);
        }

        invitation.status = mapToStatus(REVOKE);
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(invitation);
    }

    @Transactional
    public void deleteAll() {
        tenantInvitationRepository.deleteAll();
    }

    // -------------------------
    // Helpers
    // -------------------------


    private void sendInvitationNotifications(TenantInvitationResponse response) {

        // Invitee mail (only on ACCEPT)
        if (response.status == InvitationStatus.ACCEPTED) {

            var tenantDetailsUrl = uiBaseUrl + "/tenants/" + response.tenantId + "/details";

            try {
                mailerService.sendInvitationAcceptedToInvitee(
                        List.of(response.email),
                        response.tenantName,
                        response.role,
                        tenantDetailsUrl
                );
            } catch (Exception e) {
                Log.warn("Invitation accepted email to invitee failed: " + response.email, e);
            }
        }

        // Admins mail (ACCEPT/REJECT)
        try {

            var admins = groupManagementService.getTenantMembersByRole(response.tenantName, "admin");

            if (admins == null) {
                Log.warn("AGM returned null admins list");
                admins = List.of();
            }

            // Build emails (minimal filtering: only null/blank)
            var adminEmails = admins.stream()
                    .map(u -> u.email)
                    .filter(e -> e != null && !e.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();

            if (adminEmails.isEmpty()) {
                Log.warnf("No admin emails found for tenant=%s (invitee=%s). No email will be sent.", response.tenantName, response.email);
                return;
            }

            var tenantMembersUrl = uiBaseUrl + "/tenants/" + response.tenantId + "/members";

            mailerService.sendInvitationResponseToAdmins(
                    adminEmails,
                    response.tenantName,
                    response.email,
                    response.role,
                    response.status,
                    tenantMembersUrl
            );

        } catch (Exception e) {
            Log.warn("Invitation response email to tenant admins failed.", e);
        }
    }

    private void enforceInviteOwnership(String invitationEmail, String userEmail) {
        if (invitationEmail == null || userEmail == null || !invitationEmail.equalsIgnoreCase(userEmail)) {
            throw new ForbiddenException("Access denied.");
        }
    }

    private void enforcePending(InvitationStatus status) {

        if (status == null) {
            throw new WebApplicationException("Invitation status is missing.", 409);
        }

        if (status == InvitationStatus.PENDING) {
            return;
        }

        var message = switch (status) {
            case ACCEPTED -> "Invitation already accepted.";
            case REJECTED -> "Invitation already rejected.";
            case REVOKED  -> "Invitation has been revoked.";
            default       -> "Invitation already responded.";
        };

        throw new WebApplicationException(message, 409);
    }

    private InvitationStatus mapToStatus(InvitationAction action) {
        return switch (action) {
            case ACCEPT -> InvitationStatus.ACCEPTED;
            case REJECT -> InvitationStatus.REJECTED;
            case REVOKE ->  InvitationStatus.REVOKED;
        };
    }
}