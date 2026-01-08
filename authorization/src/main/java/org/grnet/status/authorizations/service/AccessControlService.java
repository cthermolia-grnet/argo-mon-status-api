package org.grnet.status.authorizations.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.entitlements.Entitlement;
import org.grnet.status.authorizations.entitlements.OIDCEntitlementService;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AccessControlService {

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String parentGroup;

    @Inject
    OIDCEntitlementService oidc;

    /**
     * Performs authorization based on:
     * - group from @CheckEntitlements
     * - role (viewer/admin)
     * - pathId (from URL)
     * - entitlement hierarchy: [parent, subgroup, id]
     */
    public boolean hasAccess(String annotatedGroup, String requiredRole, String pathId, GroupIdResolver resolver) {

        var entitlements = oidc.fetchEntitlements();

        if (isSuperAdmin(entitlements)) {
            return true;
        }

        // GLOBAL GROUP ACCESS (2-level entitlements): namespace:<group>:role=...
        boolean hasGlobal = entitlements.stream().anyMatch(e -> {
            var h = e.getHierarchy();
            return h != null && h.size() == 2 &&
                    annotatedGroup.equals(h.get(1)) &&
                    roleMatches(e.getRole(), requiredRole);
        });

        if (hasGlobal) {
            return true;
        }

        //GROUP-LEVEL ACCESS (list endpoints, no ID provided)
        if (pathId == null) {
            return entitlements.stream().anyMatch(e -> {
                var h = e.getHierarchy();
                return h != null && h.size() >= 2 &&
                        annotatedGroup.equals(h.get(1)) &&
                        roleMatches(e.getRole(), requiredRole);
            });
        }

        //ID-LEVEL ACCESS (endpoints with {id})
        for (Entitlement e : entitlements) {

            var hierarchy = e.getHierarchy();
            if (hierarchy.size() < 3) {
                continue;
            }

            var subgroup = hierarchy.get(1);
            var subgroupValue = hierarchy.get(2);

            if (!subgroup.equals(annotatedGroup)) continue;

            var resolvedId = resolver.resolve(subgroupValue);

            var idMatch = resolvedId != null && resolvedId.equals(pathId);
            var roleMatch = roleMatches(e.getRole(), requiredRole);

            if (idMatch && roleMatch) {
                return true;
            }
        }

        return false;
    }

    public List<String> resolveAccessibleGroups(String group, GroupIdResolver resolver) {

        var entitlements = oidc.fetchEntitlements();

        // super admin sees ALL
        if (isSuperAdmin(entitlements)) {
            return null;
        }

        List<String> ids = new ArrayList<>();

        for (Entitlement e : entitlements) {

            var hierarchy = e.getHierarchy();
            if (hierarchy.size() < 3) continue;

            var subgroup = hierarchy.get(1);
            var subgroupValue = hierarchy.get(2);

            if (!group.equals(subgroup)) continue;

            var resolved = resolver.resolve(subgroupValue);

            if (resolved != null) {
                ids.add(resolved);
            }
        }

        return ids;
    }


    private boolean roleMatches(String userRole, String requiredRole) {
        if (requiredRole == null || requiredRole.isBlank()) {
            return true;
        }

        if ("member".equals(requiredRole)) {
            return "member".equals(userRole) || "viewer".equals(userRole) || "admin".equals(userRole);
        }

        if ("viewer".equals(requiredRole)) {
            return "viewer".equals(userRole) || "admin".equals(userRole);
        }

        if ("admin".equals(requiredRole)) {
            return "admin".equals(userRole);
        }

        return requiredRole.equals(userRole);
    }

    /**
     * A super_admin entitlement has no hierarchy.
     */
    public boolean isSuperAdmin() {
        return isSuperAdmin(oidc.fetchEntitlements());
    }

    private boolean isSuperAdmin(List<Entitlement> entitlements) {
        return entitlements.stream()
                .anyMatch(e -> "super_admin".equals(e.getRole()));
    }
}