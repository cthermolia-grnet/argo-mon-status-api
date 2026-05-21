package org.grnet.status.services;

import io.quarkus.oidc.TokenIntrospection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AccessControlService {
    //
    @Inject
    EntitlementProvider entitlementProvider;

    @Inject
    TokenIntrospection tokenIntrospection;

    @ConfigProperty(name = "api.auth.entitlements.namespace")
    String namespace;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;


    /**
     * Resolves all accessible subgroup identifiers for the specified group name.
     *
     * @return list of accessible subgroup identifiers
     */
    public List<String> resolveAccessibleGroupsByName(String role,String resource) {

        var entitlements = fetchEntitlementsBySubGroupId(role,resource);

        return entitlements
                .stream()
                .map(Entitlement::getHierarchy)
                .filter(h -> h != null && !h.isEmpty())
                .map(h -> h.get(h.size() - 1))
                .collect(Collectors.toList());
    }

    /**
     * Determines whether the user's role satisfies the required role.
     *
     * @param userRole     role from entitlement
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
        return entitlementProvider.isSuperAdmin();
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
     * Determines whether an entitlement hierarchy covers the target hierarchy.
     *
     * @param entitlementHierarchy entitlement hierarchy
     * @param targetHierarchy      target hierarchy
     * @return true if entitlement hierarchy covers target hierarchy
     */
    public boolean hierarchyCovers(List<String> entitlementHierarchy, List<String> targetHierarchy) {

        if (entitlementHierarchy.size() > targetHierarchy.size()) return false;
        for (int i = 0; i < entitlementHierarchy.size(); i++) {
            if (!entitlementHierarchy.get(i).equals(targetHierarchy.get(i))) return false;
        }

        return true;
    }

    /**
     * Retrieves and parses entitlements from the authenticated OIDC token filtered by subgroup.
     *
     * @return list of parsed entitlements
     */
    public List<Entitlement> fetchEntitlementsBySubGroupId(String role, String resource) {

        var arr = tokenIntrospection.getJsonObject().getJsonArray("entitlements");

        if (arr == null) {
            return Collections.emptyList();
        }

        var raws = arr.stream()
                .map(v -> v.toString().replace("\"", ""))
                .filter(s -> s.startsWith(namespace))           // filter by namespace
                .map(s -> s.replace(namespace + ":", ""))
                .filter(s -> s.startsWith("group:" + parentGroup + ":" +role + ":" + resource))
                .collect(Collectors.toList());

        return EntitlementUtils.parseEntitlements(raws);
    }
}
