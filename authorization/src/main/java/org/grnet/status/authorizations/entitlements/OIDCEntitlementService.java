package org.grnet.status.authorizations.entitlements;

import io.quarkus.oidc.TokenIntrospection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OIDCEntitlementService {

    @Inject
    TokenIntrospection tokenIntrospection;

    @ConfigProperty(name = "api.auth.entitlements.namespace")
    String namespace;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String parentGroup;


    /**
     * Retrieves and parses all entitlements from the authenticated OIDC token.
     *
     * @return list of parsed entitlements
     */
    public List<Entitlement> fetchEntitlements() {

        var arr = tokenIntrospection.getJsonObject().getJsonArray("entitlements");

        if (arr == null) {
            return Collections.emptyList();
        }

        var raws = arr.stream()
                .map(v -> v.toString().replace("\"", ""))
                .filter(s -> s.startsWith(namespace))           // filter by namespace
                .map(s -> s.replace(namespace + ":", ""))
                .collect(Collectors.toList());

        return EntitlementUtils.parseEntitlements(raws);
    }

    /**
     * Retrieves and parses entitlements from the authenticated OIDC token filtered by subgroup.
     *
     * @param subgroup subgroup identifier
     * @return list of parsed entitlements
     */
    public List<Entitlement> fetchEntitlementsBySubGroupId(String subgroup) {

        var arr = tokenIntrospection.getJsonObject().getJsonArray("entitlements");

        if (arr == null) {
            return Collections.emptyList();
        }

        var raws = arr.stream()
                .map(v -> v.toString().replace("\"", ""))
                .filter(s -> s.startsWith(namespace))           // filter by namespace
                .map(s -> s.replace(namespace + ":", ""))
                .filter(s -> s.startsWith( "group:"+parentGroup+":"+subgroup))
                .collect(Collectors.toList());

        return EntitlementUtils.parseEntitlements(raws);
    }

    /**
     * Parses a provided list of raw entitlement strings filtered by subgroup.
     *
     * @param entitlements raw entitlement strings
     * @param subgroup subgroup identifier
     * @return list of parsed entitlements
     */
    public List<Entitlement> parseEntitlementsBySubGroup(List<String> entitlements, String subgroup) {

        if (entitlements == null) {
            return Collections.emptyList();
        }

        var raws = entitlements.stream()
                .map(v -> v.replace("\"", ""))
                .filter(s -> s.startsWith(namespace))           // filter by namespace
                .map(s -> s.replace(namespace + ":", ""))
                .filter(s -> s.startsWith( "group:"+parentGroup+":"+subgroup))
                .collect(Collectors.toList());

        return EntitlementUtils.parseEntitlements(raws);
    }
}
