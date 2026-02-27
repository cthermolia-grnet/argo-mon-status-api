package org.grnet.status.authorizations.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.grnet.status.authorizations.dtos.UserGroupInfoDto;
import org.grnet.status.authorizations.entitlements.Entitlement;
import org.grnet.status.authorizations.entitlements.OIDCEntitlementService;

import java.util.List;

/**
 * Service responsible for transforming OIDC entitlements into user group information.
 */
@ApplicationScoped
public class UserEntitlementsService {

    @Inject
    OIDCEntitlementService oidc;

    /**
     * Retrieves the authenticated user's entitlements and converts them to group information.
     *
     * @return list of user group information
     */
    public List<UserGroupInfoDto> getUserEntitlements() {

        var entitlements = oidc.fetchEntitlements();

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
     * @param subgroup subgroup identifier
     * @return list of user group information
     */
    public List<UserGroupInfoDto> parseLocalEntitlements(List<String> localEntitlements, String subgroup) {

        var entitlements = oidc.parseEntitlementsBySubGroup(localEntitlements, subgroup);

        return entitlements.stream()
                .map(this::toInfo)
                .toList();
    }
}

