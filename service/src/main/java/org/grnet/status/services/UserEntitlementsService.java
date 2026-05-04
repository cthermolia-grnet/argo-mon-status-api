package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.UserGroupInfoDto;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;

import java.util.List;

@ApplicationScoped
public class UserEntitlementsService {

    @Inject
    EntitlementProvider entitlementProvider;

    @Inject
    AccessControlService accessControlService;
    /**
     * Retrieves the authenticated user's entitlements and converts them to group information.
     *
     * @return list of user group information
     */
    public List<UserGroupInfoDto> getUserEntitlements() {

        var entitlements = entitlementProvider.fetchEntitlements();

        return entitlements.stream()
                .map(this::toInfo)
                .toList();
    }

    /**
     * Converts an entitlement into a user group information DTO.
     *
     * @param e entitlement
     * @return user group information
     */
    private UserGroupInfoDto toInfo(Entitlement e) {

        var info = new UserGroupInfoDto();
        info.role = e.getRole();

        var hierarchy = e.getHierarchy();

        if (hierarchy != null && !hierarchy.isEmpty()) {
            // subgroup = last element before :role
            info.name = hierarchy.get(hierarchy.size() - 1);
        }

        return info;
    }

    /**
     * Parses a list of local entitlement strings filtered by subgroup and converts them to group information.
     *
     * @param localEntitlements raw entitlement strings
     * @return list of user group information
     */
    public List<UserGroupInfoDto> parseLocalEntitlements(List<String> localEntitlements, String role, String resource) {

      var entitlements=  accessControlService.fetchEntitlementsBySubGroupId(role,resource);

        return entitlements.stream()
                .map(this::toInfo)
                .toList();
    }
}