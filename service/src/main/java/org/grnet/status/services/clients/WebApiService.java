package org.grnet.status.services.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiRequest;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiResponse;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.utils.EncryptUtil;

@ApplicationScoped
public class WebApiService {
    @Inject
    EncryptUtil encryptUtil;
    @ConfigProperty(name = "web.api.access.token")
    String accessToken;
    @ConfigProperty(name = "web.api.url")
    String webapi;
    @Inject
    ArgoWebApiClientFactory argoWebApiClientFactory;

    @Inject
    TenantRepository tenantRepository;

    public TenantWebApiGetResponse retrieveTenantWebApi(String id) throws JsonProcessingException {
        TenantWebApiGetResponse webApiResponse = null;
        try {

            var client = produceClient();
            return client.getTenant(accessToken, id);

        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
            throw new WebApplicationException("tenant with id " + id + "failed in api " + message, status);
        }
    }

    public void deleteTenant(String tenantId) throws JsonProcessingException {

        try {
            var client = produceClient();
            client.deleteTenant(tenantId, accessToken);
        } catch (Exception rollbackEx) {
            // Log rollback failure, but do not mask original exception
            System.err.println("Rollback failed for tenant id " + tenantId + ": " + rollbackEx.getMessage());
        }
    }

    public TenantWebApiCreateResponse createTenantInWebApi(TenantWebApiRequest webApiRequest) {
        try {
            var client = produceClient();
            return client.createTenant(accessToken, webApiRequest);
        } catch (WebApplicationException e) {

            WebApplicationException wae = (WebApplicationException) e;
            int status = wae.getResponse().getStatus();
            var message = wae.getMessage();
            if (status == 409) {
                var optTenant = tenantRepository.fetchTenantByName(webApiRequest.info.name);
                if (optTenant.isPresent()) {
                    message = message + ". Existing tenant in Argo Mon Status API has id: " + optTenant.get().id;
                } else {
                    message = message + ". Tenant exists in Argo Web Api but not in Argo Mon Status API";
                }
            }
            throw new WebApplicationException(message, status);
        }
    }
    private ArgoWebApiClient produceClient() {
        return argoWebApiClientFactory.buildClient(webapi);
    }
    public TenantWebApiResponse updateTenantWebApi(TenantWebApiRequest webApiRequest, String id) {
        try {

            var client = produceClient();
            //return client.updateTenant(id, accessToken, webApiRequest);
            client.updateTenantInfo(id,accessToken,webApiRequest);
            client.updateTenantTopology(id,accessToken,webApiRequest);
            return client.updateTenantDBConf(id,accessToken,webApiRequest);

        } catch (Exception e) {
            throw new WebApplicationException("Remote API update failed: " + e.getMessage(), 502);
        }
    }
}
