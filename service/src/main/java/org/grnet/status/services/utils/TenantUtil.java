package org.grnet.status.services.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.services.clients.ArgoWebApiClient;

@ApplicationScoped
public class TenantUtil {

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    public String getArgoEngineKey(String accessToken, String id) {
        var tenant = argoWebApiClient.getTenant(accessToken, id);

        if (tenant == null) {
            throw new NotFoundException("Tenant with id: " + id + " not found");
        }
        String apiKey = tenant.getData().get(0).getUsers().stream()
                .filter(u -> u.getName() != null && u.getName().contains("argo_engine"))
                .map(TenantWebApiGetResponse.User::getApi_key)
                .findFirst()
                .orElse(null);

        return apiKey;
    }

}
