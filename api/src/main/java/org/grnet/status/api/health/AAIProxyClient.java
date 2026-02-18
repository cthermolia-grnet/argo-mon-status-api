package org.grnet.status.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/.well-known")
@RegisterRestClient(configKey = "keycloak-health-check")
@ApplicationScoped
/**
 * This HTTP client communicates with Eosc Infrastructure Proxy.
 */
public interface AAIProxyClient {

    @GET
    @Path("/openid-configuration")
    Response configurations();
}
