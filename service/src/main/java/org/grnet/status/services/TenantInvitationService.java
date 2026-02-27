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
import org.grnet.status.enums.InvitationAction;
import org.grnet.status.enums.InvitationStatus;
import org.grnet.status.exceptions.BadRequestException;
import org.grnet.status.mappers.TenantInvitationMapper;
import org.grnet.status.repositories.TenantInvitationRepository;
import org.grnet.status.repositories.TenantRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.grnet.status.enums.InvitationAction.REVOKE;


/**
 * Service responsible for managing tenant invitations.
 */
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


    /**
     * Creates a new tenant invitation or resends an existing pending invitation.
     *
     * @param tenantId tenant identifier
     * @param request invitation request
     * @param createdBy creator identifier
     * @return invitation response
     */
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
     * Retrieves paginated invitations for the specified user.
     *
     * @param userEmail user email
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @return paginated list of invitations
     */
    public PageResource<TenantInvitationResponse> getAllInvitationsByUser(String userEmail, int page, int size, UriInfo uriInfo) {

        var invitations = tenantInvitationRepository.findAllByEmail(userEmail, page , size);

        return new PageResource<>(invitations, TenantInvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }

    /**
     * Retrieves a tenant invitation by its identifier for the specified user.
     *
     * @param id invitation identifier
     * @param userEmail user email
     * @return invitation response
     */
    public TenantInvitationResponse getInvitationById(String id, String userEmail) {

        var invitations = tenantInvitationRepository.findById(id);

        enforceInvitationOwnership(invitations.email, userEmail);

        return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(invitations);
    }

    /**
     * Retrieves paginated invitations with optional search and sorting.
     *
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @return paginated list of invitations
     */
    public PageResource<TenantInvitationResponse> getInvitationsByPageAndSize (String search, String sort, String order, int page, int size, UriInfo uriInfo) {

        var invitations = tenantInvitationRepository.fetchInvitationsByPageAndSize(search, sort, order, page, size);

        return new PageResource<>(invitations, TenantInvitationMapper.INSTANCE.listToDtos(invitations.list()), uriInfo);
    }



    /**
     * Retrieves paginated invitations for a specific tenant.
     *
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @param tenantId tenant identifier
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @return paginated list of invitations
     */
    public PageResource<TenantInvitationResponse> getInvitationsByTenantByPageAndSize (String search, String sort, String order, String tenantId, int page, int size, UriInfo uriInfo) {

        var tenantInvitations = tenantInvitationRepository.fetchInvitationsByTenantByPageAndSize(search, sort, order, tenantId, page, size);

        return new PageResource<>(tenantInvitations, TenantInvitationMapper.INSTANCE.listToDtos(tenantInvitations.list()), uriInfo);
    }

    /**
     * Processes an invitation response for the authenticated user.
     *
     * @param invitationId invitation identifier
     * @param request invitation action request
     * @param userEmail user email
     * @param userUniqueId user unique identifier
     * @param username username used for group assignment
     * @return invitation response
     */
    public TenantInvitationResponse respondToInvitation(String invitationId,
                                                        TenantInvitationActionResponse request,
                                                        String userEmail,
                                                        String userUniqueId,
                                                        String username) {

        var invitation = tenantInvitationRepository.findById(invitationId);

        enforceInvitationOwnership(invitation.email, userEmail);
        enforcePending(invitation.status);

        if (request.action == InvitationAction.ACCEPT) {
            try {
                Log.info("Adding user to tenant group.");
                groupManagementService.addUserToTenantGroup(invitation.tenant.name, username, invitation.role);
            } catch (Exception e) {
                Log.warn("Accepting invitation... Failed to add user to tenant group.", e);
                // invitation stays PENDING
                throw new WebApplicationException(
                     "Accepting invitation... "+   "The user could not be added to the tenant group at the moment.", 503
                );
            }
        }

        var result = respond(invitationId, request, userEmail, userUniqueId);

        try {
            Log.info("Sending invitation notifications.");
            sendInvitationNotifications(result);
        } catch (Exception e) {
            Log.warn("Notifying for invitation response... Invitation notifications failed.", e);
        }

        return result;
    }

    /**
     * Revokes a tenant invitation.
     *
     * @param tenantId tenant identifier
     * @param invitationId invitation identifier
     * @param userUniqueId user unique identifier
     * @return invitation response
     */
    public TenantInvitationResponse revokeInvitation(String tenantId, String invitationId, String userUniqueId) {

        return revoke(tenantId, invitationId, userUniqueId);
    }


    /**
     * Updates the invitation status based on the provided action.
     *
     * @param invitationId invitation identifier
     * @param request invitation action request
     * @param userEmail user email
     * @param userUniqueId user unique identifier
     * @return invitation response
     */
    @Transactional
    public TenantInvitationResponse respond(String invitationId,
                                            TenantInvitationActionResponse request,
                                            String userEmail,
                                            String userUniqueId) {

        var invitation = tenantInvitationRepository.findById(invitationId);

        var newStatus = mapToStatus(request.action);

        invitation.status = newStatus;
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(invitation);
    }

    /**
     * Updates the invitation status to revoked for the specified tenant.
     *
     * @param tenantId tenant identifier
     * @param invitationId invitation identifier
     * @param userUniqueId user unique identifier
     * @return invitation response
     */
    @Transactional
    public TenantInvitationResponse revoke(String tenantId, String invitationId, String userUniqueId) {

        var invitation = tenantInvitationRepository.findById(invitationId);

        if (!Objects.equals(invitation.tenant.getId(), tenantId)) {
            throw new WebApplicationException("Revoking invitation... Invitation is not linked to this tenant.", 409);
        }

        invitation.status = mapToStatus(REVOKE);
        invitation.respondedAt = Instant.now();
        invitation.respondedBy = userUniqueId;

        return TenantInvitationMapper.INSTANCE.tenantInvitationToDto(invitation);
    }

    /**
     * Deletes all tenant invitations.
     */
    @Transactional
    public void deleteAll() {
        tenantInvitationRepository.deleteAll();
    }

    // -------------------------
    // Helpers
    // -------------------------

    /**
     * Sends invitation response notifications to invitee and tenant administrators.
     *
     * @param response invitation response
     */
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

    /**
     * Validates that the invitation belongs to the authenticated user.
     *
     * @param invitationEmail invitation email
     * @param userEmail user email
     */
    private void enforceInvitationOwnership(String invitationEmail, String userEmail) {

        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("Validating invitation... Authenticated user email is missing.");
        }

        if (invitationEmail == null || invitationEmail.isBlank()) {
            throw new WebApplicationException("Validating invitation... Invitation has no associated email.", 500);
        }

        if (!invitationEmail.equalsIgnoreCase(userEmail)) {
            throw new ForbiddenException("Validating invitation... This invitation does not belong to the authenticated user.");
        }
    }

    /**
     * Validates that the invitation status is pending.
     *
     * @param status invitation status
     */
    private void enforcePending(InvitationStatus status) {

        if (status == null) {
            throw new WebApplicationException("Validating invitation's status... Invitation status is missing.", 409);
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

    /**
     * Maps an invitation action to the corresponding invitation status.
     *
     * @param action invitation action
     * @return invitation status
     */
    private InvitationStatus mapToStatus(InvitationAction action) {
        return switch (action) {
            case ACCEPT -> InvitationStatus.ACCEPTED;
            case REJECT -> InvitationStatus.REJECTED;
            case REVOKE ->  InvitationStatus.REVOKED;
        };
    }
}