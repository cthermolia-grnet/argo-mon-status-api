package org.grnet.status.services.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.argo.ArgoWebApiErrorResponse;
import org.grnet.status.dtos.readiness.WebApiTenantReadiness;
import org.grnet.status.dtos.report.WebApiReportResponse;
import org.grnet.status.dtos.tenant.node.*;
import org.grnet.status.dtos.tenant.webapi.*;
import org.grnet.status.dtos.topology.FeedTopologyDto;
import org.grnet.status.dtos.topology.WebApiFeedsTopologyResponse;
import org.grnet.status.repositories.TenantRepository;
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

    private static final Logger LOG = Logger.getLogger(WebApiService.class);

    public TenantWebApiGetResponse retrieveTenantWebApi(String id) {

        try {
            return argoWebApiClient.getTenant(accessToken, id);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Tenant", id);

            throw new WebApplicationException(
                    "Retrieving Tenant... tenant with id: " + id + " failed in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Retrieving Tenant failed in Argo Web Api. tenantId=%s",
                    id
            );

            throw new WebApplicationException(
                    "Retrieving Tenant... tenant with id: " + id + " failed in Argo Web Api",
                    500
            );
        }
    }

    public void deleteTenant(String tenantId) {

        try {

            argoWebApiClient.deleteTenant(tenantId, accessToken);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Deleting Tenant", tenantId);

            throw new WebApplicationException(
                    "Deleting Tenant... failed to delete tenant with id: " + tenantId + " from Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Deleting Tenant failed in Argo Web Api. tenantId=%s",
                    tenantId
            );

            throw new WebApplicationException(
                    "Deleting Tenant... failed to delete tenant with id: " + tenantId + " from Argo Web Api",
                    500
            );
        }
    }

    public TenantWebApiCreateResponse createTenantInWebApi(TenantWebApiRequest webApiRequest) {

        try {

            return argoWebApiClient.createTenant(accessToken, webApiRequest);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Creating Tenant", webApiRequest.info.name);

            var message = e.getMessage();

            if (status == 409) {

                var optTenant = tenantRepository.fetchTenantByName(webApiRequest.info.name);

                if (optTenant.isPresent()) {

                    message = "Creating Tenant... Tenant already exists in Argo Monitoring Status with id: "
                            + optTenant.get().id;

                } else {

                    message = "Creating Tenant... Tenant exists in Argo Web Api but not in Argo Monitoring Status";
                }
            }

            throw new WebApplicationException(message, status);

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Creating Tenant failed in Argo Web Api. tenantName=%s",
                    webApiRequest.info.name
            );

            throw new WebApplicationException(
                    "Creating Tenant... failed in Argo Web Api",
                    500
            );
        }
    }

    public Status updateTenantWebApi(TenantWebApiRequest webApiRequest, String id) {

        try {

            argoWebApiClient.updateTenantInfo(id, accessToken, webApiRequest);
            argoWebApiClient.updateTenantTopology(id, accessToken, webApiRequest);

            var tenantNode = new TenantWebApiNodeRequest();
            tenantNode.node = webApiRequest.node;

            updateTenantNodeWebApi(id, tenantNode);

            return argoWebApiClient.updateTenantDBConf(id, accessToken, webApiRequest);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Updating Tenant", id);

            throw new WebApplicationException(
                    "Updating Tenant... failed to update tenant with id: " + id + " in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Updating Tenant failed in Argo Web Api. tenantId=%s",
                    id
            );

            throw new WebApplicationException(
                    "Updating Tenant... failed to update tenant with id: " + id + " in Argo Web Api",
                    500
            );
        }
    }

    public WebApiNodeResponse updateTenantNodeWebApi(String tenantId,
                                                     TenantWebApiNodeRequest request) {

        LOG.info("Updating Tenant Node...");
        LOG.infof("REQUEST NODE VALUE = %s", request == null ? null : request.node);

        try {

            if (request != null && Boolean.TRUE.equals(request.node)) {


                return argoWebApiClient.setTenantNode(tenantId, accessToken);
            }
            return argoWebApiClient.unsetTenantNode(tenantId, accessToken);


        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Updating Tenant Node", tenantId);

            throw new WebApplicationException(
                    "Updating Tenant Node... failed to update tenant node for tenant with id: "
                            + tenantId + " in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Updating Tenant Node failed in Argo Web Api. tenantId=%s",
                    tenantId
            );

            throw new WebApplicationException(
                    "Updating Tenant Node... failed to update tenant node for tenant with id: "
                            + tenantId + " in Argo Web Api",
                    500
            );
        }
    }

    public WebApiTenantReadiness retrieveTenantReadinessWebApi(String id) {

        try {

            return argoWebApiClient.getTenantReadiness(id, accessToken);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Tenant Readiness", id);

            throw new WebApplicationException(
                    "Retrieving Tenant Readiness... tenant with id: " + id + " failed in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Retrieving Tenant Readiness failed in Argo Web Api. tenantId=%s",
                    id
            );

            throw new WebApplicationException(
                    "Retrieving Tenant Readiness... tenant with id: " + id + " failed in Argo Web Api",
                    500
            );
        }
    }

    public TenantWebApiGetResponse retrieveTenantsWebApi() {

        try {

            return argoWebApiClient.getTenants(accessToken);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Tenants", "all");

            throw new WebApplicationException(
                    "Retrieving Tenants... failed in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.error(e);

            throw new WebApplicationException(
                    "Retrieving Tenants... failed in Argo Web Api",
                    500
            );
        }
    }

    public void validateTenantInitialized(String tenantId, String resourceName) {

        LOG.info("Checking if Tenant is initialized...");

        var tenant = argoWebApiClient.getTenant(accessToken, tenantId);
        var dbConf = tenant.getData().get(0).getDb_conf();
        var mongodbReady = dbConf != null && !dbConf.isEmpty();

        if (!mongodbReady) {

            throw new WebApplicationException(
                    resourceName + " are not available. The tenant is still initializing.",
                    400
            );
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

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Updating Report", reportId);

            throw new WebApplicationException(
                    "Updating Report... failed to set node report with id: "
                            + reportId + " in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Updating Report failed in Argo Web Api. reportId=%s",
                    reportId
            );

            throw new WebApplicationException(
                    "Updating Report... failed to set node report with id: "
                            + reportId + " in Argo Web Api",
                    500
            );
        }
    }

    public WebApiNodeAvailabilityResponse retrieveNodeAvailability(String nodeName, String item,String date, String startTime, String endTime, String startDate, String endDate, String granularity) {
        try {
            if (StringUtils.isBlank(item)) {
                return argoWebApiClient.getNodeAvailabilityCapability(accessToken, nodeName, date, startTime, endTime, startDate, endDate, granularity);
            }

            return argoWebApiClient.getNodeAvailabilityCapabilityByService(
                    accessToken, nodeName, item, date, startTime, endTime, startDate, endDate, granularity);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Node Availability", nodeName);

            throw new WebApplicationException(
                    "Retrieving Node Availability... node with name " + nodeName + " failed in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Retrieving Node Availability failed in Argo Web Api. nodeName=%s, item=%s",
                    nodeName,
                    item
            );

            throw new WebApplicationException(
                    "Retrieving Node Availability... node with name " + nodeName + " failed in Argo Web Api",
                    500
            );
        }
    }
    public WebApiNodeStatusResponse retrieveNodeStatus(String nodeName, String item, String startTime, String endTime, Boolean history) {
        try {
            if (StringUtils.isBlank(item)) {
                return argoWebApiClient.getNodeStatus(accessToken, nodeName, startTime, endTime, history);
            }

            return argoWebApiClient.getNodeStatusByService(accessToken, nodeName, item, startTime, endTime, history);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Node Status", nodeName);

            throw new WebApplicationException(
                    "Retrieving Node Status... node with name " + nodeName + " failed in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Retrieving Node Status failed in Argo Web Api. nodeName=%s, item=%s",
                    nodeName,
                    item
            );

            throw new WebApplicationException(
                    "Retrieving Node Status... node with name " + nodeName + " failed in Argo Web Api",
                    500
            );
        }
    }

    public WebApiFeedsTopologyResponse retrieveFeedTopologyWebApi(String tenantId) {
        try {

            return argoWebApiClient.getFeedTopology(accessToken, tenantId);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Feed Topology", tenantId);

            if (status == 404) {

                throw new WebApplicationException(
                        "Retrieving Feed Topology... topology feed has not been configured for tenant with id: "
                                + tenantId,
                        404
                );
            }

            throw new WebApplicationException(
                    "Retrieving Feed Topology... failed to retrieve topology feed for tenant with id: "
                            + tenantId + " from Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Retrieving Feed Topology failed in Argo Web Api. tenantId=%s",
                    tenantId
            );

            throw new WebApplicationException(
                    "Retrieving Feed Topology... failed to retrieve topology feed for tenant with id: "
                            + tenantId + " from Argo Web Api",
                    500
            );
        }
    }

    public WebApiFeedsTopologyResponse updateFeedTopologyWebApi(String tenantId, FeedTopologyDto request) {
        try {

            return argoWebApiClient.updateFeedTopology(accessToken, tenantId, request);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Updating Feed Topology", tenantId);

            throw new WebApplicationException(
                    "Updating Feed Topology... failed to update topology feed for tenant with id: "
                            + tenantId + " in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Updating Feed Topology failed in Argo Web Api. tenantId=%s",
                    tenantId
            );

            throw new WebApplicationException(
                    "Updating Feed Topology... failed to update topology feed for tenant with id: "
                            + tenantId + " in Argo Web Api",
                    500
            );
        }
    }

    public WebApiNodeSummaryResponse retrieveNodeSummary(String nodeName, String item, String startDate, String endDate, String granularity) {

        try {
            return argoWebApiClient.getNodeSummaryCapability(accessToken, nodeName, item, startDate, endDate, granularity);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Node Summary", nodeName);

            throw new WebApplicationException(
                    "Retrieving Node Summary... node with name " + nodeName +
                            " and service " + item +
                            " failed in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Retrieving Node Summary failed in Argo Web Api. nodeName=%s, item=%s",
                    nodeName,
                    item
            );

            throw new WebApplicationException(
                    "Retrieving Node Summary... node with name " + nodeName +
                            " and service " + item +
                            " failed in Argo Web Api",
                    500
            );
        }
    }

    public TenantWebApiGroupResultsResponse retrieveGroupResults(String groupName, String id, String date, String period, String startTime, String endTime, String startDate, String endDate, String granularity, String report) {

        try {
            if (StringUtils.isBlank(groupName)) {
                return argoWebApiClient.getGroupResultsSuperAdmin(accessToken, id, date, period, startTime, endTime, startDate, endDate, granularity, report);
            }

            return argoWebApiClient.getGroupResultsByGroupSuperAdmin(accessToken, id, groupName, date, period, startTime, endTime, startDate, endDate, granularity, report);

        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();

            var errorMessage = logArgoError(e, "Retrieving Group Results", StringUtils.defaultIfBlank(groupName, "all"));

            throw new WebApplicationException(
                    "Retrieving Group Results... " + errorMessage,
                    status
            );

        } catch (RuntimeException e) {
            LOG.errorf(e, "Retrieving Group Results failed in Argo Web Api. groupName=%s, report=%s", groupName, report);

            throw new WebApplicationException(
                    "Retrieving Group Results... failed in Argo Web Api",
                    500
            );
        }
    }

    public TenantWebApiGroupStatusResponse retrieveGroupStatus(String groupName, String id, String startTime, String endTime, Boolean history, String report) {

        try {
            if (StringUtils.isBlank(groupName)) {
                return argoWebApiClient.getGroupStatusSuperAdmin(accessToken, id, startTime, endTime, history, report);
            }

            return argoWebApiClient.getGroupStatusByGroupSuperAdmin(accessToken, id, groupName, startTime, endTime, history, report);

        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();

            var errorMessage = logArgoError(e, "Retrieving Group Status", StringUtils.defaultIfBlank(groupName, "all"));

            throw new WebApplicationException(
                    "Retrieving Group Status... " + errorMessage,
                    status
            );

        } catch (RuntimeException e) {
            LOG.errorf(e, "Retrieving Group Status failed in Argo Web Api. groupName=%s, report=%s", groupName, report);

            throw new WebApplicationException(
                    "Retrieving Group Status... failed in Argo Web Api",
                    500
            );
        }
    }

    public WebApiNodeReportResponse setReportPublicWebApi(String reportId, String tenantId) {

        try {
            return argoWebApiClient.setReportPublicSuperAdmin(reportId, accessToken, tenantId);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Updating Report Visibility", reportId);

            throw new WebApplicationException(
                    "Updating Report Visibility... failed to set report with id: "
                            + reportId + " as public in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Updating Report Visibility failed in Argo Web Api. reportId=%s",
                    reportId
            );

            throw new WebApplicationException(
                    "Updating Report Visibility... failed to set report with id: "
                            + reportId + " as public in Argo Web Api",
                    500
            );
        }
    }

    public WebApiNodeReportResponse setReportPrivateWebApi(String reportId, String tenantId) {

        try {
            return argoWebApiClient.setReportPrivateSuperAdmin(reportId, accessToken, tenantId);

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Updating Report Visibility", reportId);

            throw new WebApplicationException(
                    "Updating Report Visibility... failed to set report with id: "
                            + reportId + " as private in Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Updating Report Visibility failed in Argo Web Api. reportId=%s",
                    reportId
            );

            throw new WebApplicationException(
                    "Updating Report Visibility... failed to set report with id: "
                            + reportId + " as private in Argo Web Api",
                    500
            );
        }
    }

    public WebApiReportResponse retrieveReportsWebApi(String tenantId, Boolean publicReports, Boolean privateReports) {

        try {

            return argoWebApiClient.fetchReportsSuperAdmin(accessToken, tenantId, Boolean.TRUE.equals(publicReports) ? "" : null, Boolean.TRUE.equals(privateReports) ? "" : null

            );

        } catch (WebApplicationException e) {

            int status = e.getResponse().getStatus();

            logArgoError(e, "Retrieving Reports", tenantId);

            throw new WebApplicationException(
                    "Retrieving Reports... failed to retrieve reports for tenant with id: "
                            + tenantId + " from Argo Web Api",
                    status
            );

        } catch (RuntimeException e) {

            LOG.errorf(e,
                    "Retrieving Reports failed in Argo Web Api. tenantId=%s",
                    tenantId
            );

            throw new WebApplicationException(
                    "Retrieving Reports... failed to retrieve reports for tenant with id: "
                            + tenantId + " from Argo Web Api",
                    500
            );
        }
    }


    private String logArgoError(WebApplicationException e, String operation, String identifier) {

        try {

            var body = e.getResponse().readEntity(String.class);
            var error = new ObjectMapper()
                    .readValue(body, ArgoWebApiErrorResponse.class);

            LOG.errorf(
                    "%s failed in Argo Web Api. identifier=%s, status=%s, argoMessage=%s",
                    operation,
                    identifier,
                    e.getResponse().getStatus(),
                    error.extractMessage()
            );

            return error.extractMessage();

        } catch (Exception ex) {

            LOG.errorf(
                    ex,
                    "Failed parsing Argo Web Api error response. operation=%s, identifier=%s, status=%s",
                    operation,
                    identifier,
                    e.getResponse().getStatus()
            );
        }
        return operation;
    }
}
