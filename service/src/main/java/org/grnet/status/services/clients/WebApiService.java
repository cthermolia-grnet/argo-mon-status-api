package org.grnet.status.services.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.readiness.WebApiTenantReadiness;
import org.grnet.status.dtos.tenant.node.WebApiNodeReportResponse;
import org.grnet.status.dtos.tenant.node.WebApiNodeResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiNodeRequest;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiRequest;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.ReportService;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WebApiService {
    @ConfigProperty(name = "web.api.access.token")
    String accessToken;
    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;
    @Inject
    TenantRepository tenantRepository;

    private static final Logger LOG = Logger.getLogger(ReportService.class);

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

            Log.error(e.getMessage(),e);
           throw new WebApplicationException("Retrieving Tenants... tenant with id " + id + " failed in Argo Web Api",status);

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
                    message ="Creating Tenant... Tenant already exists in Argo Monitoring Status with id" + optTenant.get().id;
                } else {
                    message ="Creating Tenant... Tenant exists in Argo Web Api but not in Argo Monitoring Status";
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

            var tenantNode = new TenantWebApiNodeRequest();
            tenantNode.node = webApiRequest.node;
            updateTenantNodeWebApi(id, tenantNode);

            return argoWebApiClient.updateTenantDBConf(id, accessToken, webApiRequest);
        } catch (Exception e) {
            throw new WebApplicationException("Updating Tenant... Failed to update tenant with id: " + id +" in Argo Web Api", 502);
        }
    }

    public WebApiNodeResponse updateTenantNodeWebApi(String tenantId, TenantWebApiNodeRequest request) {

        LOG.info("Updating Tenant Node...");
        LOG.infof("REQUEST NODE VALUE = %s", request == null ? null : request.node);

        try {
            if (Boolean.TRUE.equals(request.node)) {
                LOG.info("NODE IS TRUE");
                return argoWebApiClient.setTenantNode(tenantId, accessToken);
            }

            LOG.info("NODE IS NULL");
            return argoWebApiClient.unsetTenantNode(tenantId, accessToken);

        } catch (Exception e) {
            LOG.error("Failed updating tenant node", e);
            throw new WebApplicationException(
                    "Updating Tenant's Node information... Failed to update tenant with id: " + tenantId + " in Argo Web Api",
                    502
            );
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

            Log.error(e.getMessage(),e);
            throw new WebApplicationException("Retrieving Tenant's Readiness... tenant with id " + id + "failed in Argo Web Api", status);
        }
    }

    public void validateTenantInitialized(String tenantId, String resourceName) {

        LOG.info("Checking if Tenant is initialized...");
        var tenant = argoWebApiClient.getTenant(accessToken, tenantId);
        var dbConf = tenant.getData().get(0).getDb_conf();
        var mongodbReady = dbConf != null && !dbConf.isEmpty();

        if (!mongodbReady) {
            throw new WebApplicationException(resourceName + " are not available. The tenant is still initializing.", 400);
        }
    }

    public TenantWebApiGetResponse retrieveTenantsWebApi() throws JsonProcessingException {
        try {
            return argoWebApiClient.getTenants(accessToken);
        } catch (RuntimeException e) {
            int status = 500;
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }

            Log.error(e.getMessage(), e);
            throw new WebApplicationException("Retrieving Tenants... failed in Argo Web Api", status);
        }
    }


    /**
     * Sets the default node report in Argo Web Api.
     *
     * @param reportId report identifier
     * @return status response
     */
    public WebApiNodeReportResponse setNodeReportWebApi(String reportId, String tenantId) {
        try {
            return argoWebApiClient.setNodeReport(reportId, accessToken, tenantId);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Updating Report... Failed to set node report with id: " + reportId + " in Argo Web Api",
                    502
            );
        }
    }

}
