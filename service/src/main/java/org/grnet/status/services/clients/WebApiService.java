package org.grnet.status.services.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.readiness.WebApiTenantReadiness;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiRequest;
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
    @RestClient
    ArgoWebApiClient argoWebApiClient;
    @Inject
    TenantRepository tenantRepository;

    public TenantWebApiGetResponse retrieveTenantWebApi(String id) throws JsonProcessingException {
        TenantWebApiGetResponse webApiResponse = null;
        try {

            //  var client = produceClient();
            return argoWebApiClient.getTenant(accessToken, id);

        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
           throw new WebApplicationException("Retrieving Tenants... tenant with id " + id + " failed in Argo Web Api \n Message received is: " + message,status);

        }
    }

    public void deleteTenant(String tenantId) throws JsonProcessingException {

        try {
            //  var client = produceClient();
            argoWebApiClient.deleteTenant(tenantId, accessToken);
        } catch (Exception rollbackEx) {
            // Log rollback failure, but do not mask original exception
            System.err.println("Rollback failed for tenant id " + tenantId + ": " + rollbackEx.getMessage());
        }
    }

    public TenantWebApiCreateResponse createTenantInWebApi(TenantWebApiRequest webApiRequest) {
        try {
            // var client = produceClient();
            return argoWebApiClient.createTenant(accessToken, webApiRequest);
        } catch (WebApplicationException e) {

            WebApplicationException wae = (WebApplicationException) e;
            int status = wae.getResponse().getStatus();
            var message = wae.getMessage();
            if (status == 409) {
                var optTenant = tenantRepository.fetchTenantByName(webApiRequest.info.name);
                if (optTenant.isPresent()) {
                    message ="Creating Tenant... Tenant already exists in Argo Monitoring Status with id" + optTenant.get().id+" Message received: "+e.getMessage();
                } else {
                    message ="Creating Tenant... Tenant exists in Argo Web Api but not in Argo Monitoring Status"+" Message received: "+e.getMessage();
                }
            }
            throw new WebApplicationException(message, status);
        }
    }

    public Status updateTenantWebApi(TenantWebApiRequest webApiRequest, String id) {
        try {

            // var client = produceClient();
            //return client.updateTenant(id, accessToken, webApiRequest);
            argoWebApiClient.updateTenantInfo(id, accessToken, webApiRequest);
            argoWebApiClient.updateTenantTopology(id, accessToken, webApiRequest);
            return argoWebApiClient.updateTenantDBConf(id, accessToken, webApiRequest);
        } catch (Exception e) {
            throw new WebApplicationException("Updating Tenant... Failed to update tenant with id: " + id +" in Argo Web Api. Message received: "+e.getMessage(), 502);
        }
    }


    public WebApiTenantReadiness retrieveTenantReadinessWebApi(String id) throws JsonProcessingException {
        try {

            //  var client = produceClient();
            return argoWebApiClient.getTenantReadiness(id,accessToken);

        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
            throw new WebApplicationException("Retrieving Tenant's Readiness... tenant with id " + id + "failed in Argo Web Api. Message received: " + message, status);
        }
    }

}
