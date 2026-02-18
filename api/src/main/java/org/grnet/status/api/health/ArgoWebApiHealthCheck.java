package org.grnet.status.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.services.clients.ArgoWebApiClient;

@Readiness
@ApplicationScoped
public class ArgoWebApiHealthCheck implements HealthCheck {

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @Override
    public HealthCheckResponse call() {

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Argo Web API health check");

        try (Response response = argoWebApiClient.version()) {

                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    responseBuilder.up();
                } else {
                    responseBuilder.down().withData("error", "Cannot access the Argo Web API.");
                }
            } catch (Exception e){

            responseBuilder.down().withData("error", "Cannot access the Argo Web API.");
        }

        return responseBuilder.build();
    }
}
