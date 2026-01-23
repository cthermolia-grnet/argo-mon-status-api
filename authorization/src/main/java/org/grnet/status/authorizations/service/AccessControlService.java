package org.grnet.status.authorizations.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.entitlements.Entitlement;
import org.grnet.status.authorizations.entitlements.OIDCEntitlementService;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AccessControlService {

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

    public List<String> resolveAccessibleGroupsByName(String group) {

        var entitlements = oidc.fetchEntitlementsBySubGroupId(group);

       return entitlements
                .stream()
                .map(Entitlement::getHierarchy)
                .filter(h->h != null && !h.isEmpty())
                .map(h->h.get(h.size() - 1))
                .collect(Collectors.toList());
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

    public boolean hasAccess(String role, List<String> targetHierarchy, String group) {

        var entitlements = oidc.fetchEntitlements();

        return entitlements.stream()
                .filter(e -> e.getGroup().equals(group) && e.getRole().equals(role))
                .anyMatch(e -> hierarchyCovers(e.getHierarchy(), targetHierarchy));
    }

    public boolean hierarchyCovers(List<String> entitlementHierarchy, List<String> targetHierarchy) {

        if (entitlementHierarchy.size() > targetHierarchy.size()) return false;
        for (int i = 0; i < entitlementHierarchy.size(); i++) {
            if (!entitlementHierarchy.get(i).equals(targetHierarchy.get(i))) return false;
        }

        return true;
    }
}