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
     * Resolves all accessible subgroup identifiers for the specified group name.
     *
     * @param group group name
     * @return list of accessible subgroup identifiers
     */
    public List<String> resolveAccessibleGroupsByName(String group) {

        var entitlements = oidc.fetchEntitlementsBySubGroupId(group);

       return entitlements
                .stream()
                .map(Entitlement::getHierarchy)
                .filter(h->h != null && !h.isEmpty())
                .map(h->h.get(h.size() - 1))
                .collect(Collectors.toList());
    }


    /**
     * Determines whether the user's role satisfies the required role.
     *
     * @param userRole role from entitlement
     * @param requiredRole required role
     * @return true if role requirement is satisfied
     */
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
     * Checks whether the authenticated user has the super_admin role.
     *
     * @return true if user is super administrator
     */
    public boolean isSuperAdmin() {
        return isSuperAdmin(oidc.fetchEntitlements());
    }

    /**
     * Checks whether the provided entitlements include the super_admin role.
     *
     * @param entitlements list of entitlements
     * @return true if super administrator role is present
     */
    private boolean isSuperAdmin(List<Entitlement> entitlements) {
        return entitlements.stream()
                .anyMatch(e -> "super_admin".equals(e.getRole()));
    }

    /**
     * Evaluates access based on role and hierarchy comparison.
     *
     * @param role required role
     * @param targetHierarchy target hierarchy
     * @param group group name
     * @return true if access is granted
     */
    public boolean hasAccess(String role, List<String> targetHierarchy, String group) {

        var entitlements = oidc.fetchEntitlements();

        return entitlements.stream()
                .filter(e -> e.getGroup().equals(group) && e.getRole().equals(role))
                .anyMatch(e -> hierarchyCovers(e.getHierarchy(), targetHierarchy));
    }

    /**
     * Determines whether an entitlement hierarchy covers the target hierarchy.
     *
     * @param entitlementHierarchy entitlement hierarchy
     * @param targetHierarchy target hierarchy
     * @return true if entitlement hierarchy covers target hierarchy
     */
    public boolean hierarchyCovers(List<String> entitlementHierarchy, List<String> targetHierarchy) {

        if (entitlementHierarchy.size() > targetHierarchy.size()) return false;
        for (int i = 0; i < entitlementHierarchy.size(); i++) {
            if (!entitlementHierarchy.get(i).equals(targetHierarchy.get(i))) return false;
        }

        return true;
    }
}