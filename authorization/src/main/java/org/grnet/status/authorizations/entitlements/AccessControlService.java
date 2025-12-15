package org.grnet.status.authorizations.entitlements;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

        //GROUP-LEVEL ACCESS (list endpoints, no ID provided)
        if (annotatedGroup != null && !annotatedGroup.isBlank() && pathId == null) {
            // Allow if user has ANY entitlement matching this group + role
            return entitlements.stream().anyMatch(e -> {
                var h = e.getHierarchy();
                if (h.size() < 2) return false;

                var subgroup = h.get(1);
                return subgroup.equals(annotatedGroup) &&
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

    public List<String> resolveAccessibleGroups(GroupIdResolver resolver) {

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

            if (!"tenants".equals(subgroup)) continue;

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