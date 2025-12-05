package org.grnet.status.authorizations.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Captures path parameters for each incoming request.
 * These values are used by the CheckEntitlementsInterceptor
 * to determine authorization scope (e.g., tenantId).
 *
 * Stored in a ThreadLocal because interceptors run outside JAX-RS context.
 */
@Provider
public class RequestFilter implements ContainerRequestFilter {

    /**
     * Holds path parameters for the duration of a single request.
     */
    private static final ThreadLocal<Map<String, String>> PATH_PARAMS_HOLDER =
            new ThreadLocal<>();

    /**
     * Returns the path parameters captured for the current request.
     */
    public static Map<String, String> getPathParams() {
        return PATH_PARAMS_HOLDER.get();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Resolve path parameters from the request URI
        var pathParams = requestContext.getUriInfo().getPathParameters()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get(0)
                ));

        PATH_PARAMS_HOLDER.set(pathParams);
    }
}