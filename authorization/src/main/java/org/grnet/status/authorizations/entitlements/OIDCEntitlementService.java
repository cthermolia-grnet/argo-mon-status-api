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

    /**
     * Extracts and parses entitlements from the OIDC token.
     */
    public List<Entitlement> fetchEntitlements() {

        var arr = tokenIntrospection.getJsonObject().getJsonArray("entitlements");

        if (arr == null) {
            return Collections.emptyList();
        }

        List<String> raws = arr.stream()
                .map(v -> v.toString().replace("\"", ""))
                .filter(s -> s.startsWith(namespace))           // filter by namespace
                .map(s -> s.replace(namespace + ":", ""))
                .collect(Collectors.toList());

        return EntitlementUtils.parseEntitlements(raws);
    }
}
