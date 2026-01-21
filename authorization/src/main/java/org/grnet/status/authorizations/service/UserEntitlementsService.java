package org.grnet.status.authorizations.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.grnet.status.authorizations.dtos.UserGroupInfoDto;
import org.grnet.status.authorizations.entitlements.Entitlement;
import org.grnet.status.authorizations.entitlements.OIDCEntitlementService;

import java.util.List;

@ApplicationScoped
public class UserEntitlementsService {

    @Inject
    OIDCEntitlementService oidc;

    public List<UserGroupInfoDto> getUserEntitlements() {

        var entitlements = oidc.fetchEntitlements();

        return entitlements.stream()
                .map(this::toInfo)
                .toList();
    }

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

    public List<UserGroupInfoDto> parseLocalEntitlements(List<String> localEntitlements, String subgroup) {

        var entitlements = oidc.parseEntitlementsBySubGroup(localEntitlements, subgroup);

        return entitlements.stream()
                .map(this::toInfo)
                .toList();
    }
}

