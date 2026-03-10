package org.grnet.status.services.clients;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.argo.ArgoStatusGroupsResponse;
import org.grnet.status.dtos.profile.aggregation.AggregationProfileResponse;
import org.grnet.status.dtos.profile.metric.MetricProfileResponse;
import org.grnet.status.dtos.profile.operation.OperationProfileResponse;
import org.grnet.status.dtos.readiness.WebApiTenantReadiness;
import org.grnet.status.dtos.report.WebApiReportResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiRequest;
import org.grnet.status.dtos.topology.*;

import java.util.List;

@RegisterRestClient(configKey = "argo-web-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ArgoWebApiClient {

    @GET
    @Path("/api/v2/reports")
    WebApiReportResponse fetchReports(
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/reports")
    WebApiReportResponse fetchReportsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/reports/{id}")
    WebApiReportResponse fetchReportById(
            @PathParam("id") String id,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/reports/{id}")
    WebApiReportResponse fetchReportByIdSuperAdmin(
            @PathParam("id") String id,
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v3/status/{report}")
    ArgoStatusGroupsResponse fetchStatusGroups(
            @HeaderParam("x-api-key") String apiKey,
            @PathParam("report") String report
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v3/status/{report}")
    ArgoStatusGroupsResponse fetchStatusGroupsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @PathParam("report") String report
    ) throws WebApplicationException, ProcessingException;


    @POST
    @Path("/api/v2/admin/tenants")
    TenantWebApiCreateResponse createTenant(
            @HeaderParam("x-api-key") String apiKey,
            TenantWebApiRequest request
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/admin/tenants/{id}")
    TenantWebApiGetResponse getTenant(
            @HeaderParam("x-api-key") String apiKey,
            @PathParam("id") String id
    ) throws WebApplicationException, ProcessingException;

    @PUT
    @Path("/api/v2/admin/tenants/{id}")
    Status updateTenant(@PathParam("id") String id,
                        @HeaderParam("x-api-key") String apiKey,
                        TenantWebApiRequest request
    ) throws WebApplicationException, ProcessingException;

    @DELETE
    @Path("/api/v2/admin/tenants/{id}")
    Status deleteTenant(
            @PathParam("id") String id,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/admin/tenants")
    TenantWebApiGetResponse getTenants(
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;


    @PUT
    @Path("/api/v2/admin/tenants/{id}/info")
    Status updateTenantInfo(
            @PathParam("id") String id,
            @HeaderParam("x-api-key") String apiKey,
            TenantWebApiRequest request
    ) throws WebApplicationException, ProcessingException;


    @PUT
    @Path("/api/v2/admin/tenants/{id}/topology")
    Status updateTenantTopology(
            @PathParam("id") String id,
            @HeaderParam("x-api-key") String apiKey,
            TenantWebApiRequest request
    ) throws WebApplicationException, ProcessingException;

    @PUT
    @Path("/api/v2/admin/tenants/{id}/db-conf")
    Status updateTenantDBConf(
            @PathParam("id") String id,
            @HeaderParam("x-api-key") String apiKey, TenantWebApiRequest request
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/operations_profiles/{id}")
    OperationProfileResponse listSpecificOperationsProfiles(
            @PathParam("id") String id,
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/operations_profiles/{id}")
    OperationProfileResponse listSpecificOperationsProfilesSuperAdmin(
            @PathParam("id") String id,
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;


    @GET
    @Path("/api/v2/operations_profiles")
    OperationProfileResponse listAllOperationsProfiles(
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/operations_profiles")
    OperationProfileResponse listAllOperationsProfilesSuperAdmin(
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;


    @GET
    @Path("/api/v2/aggregation_profiles/{id}")
    AggregationProfileResponse listSpecificAggregationProfiles(
            @PathParam("id") String id,
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/aggregation_profiles/{id}")
    AggregationProfileResponse listSpecificAggregationProfilesSuperAdmin(
            @PathParam("id") String id,
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/aggregation_profiles")
    AggregationProfileResponse listAllAggregationProfiles(
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/aggregation_profiles")
    AggregationProfileResponse listAllAggregationProfilesSuperAdmin(
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/metric_profiles/{id}")
    MetricProfileResponse listSpecificMetricProfiles(
            @PathParam("id") String id,
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/metric_profiles/{id}")
    MetricProfileResponse listSpecificMetricProfilesSuperAdmin(
            @PathParam("id") String id,
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;


    @GET
    @Path("/api/v2/metric_profiles")
    MetricProfileResponse listAllMetricProfiles(
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/metric_profiles")
    MetricProfileResponse listAllMetricProfilesSuperAdmin(
            @QueryParam("date") String date,
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/admin/tenants/{id}/ready")
    WebApiTenantReadiness getTenantReadiness(
            @PathParam("id") String id,
            @HeaderParam("x-api-key") String apiKey
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/version")
    @Produces(MediaType.APPLICATION_JSON)
    Response version();

    @GET
    @Path("/api/v2/topology/groups")
    WebApiGroupTopologyResponse fetchTopologyGroupsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date
    ) throws WebApplicationException, ProcessingException;


    @GET
    @Path("/api/v2/topology/endpoints")
    WebApiEndpointTopologyResponse fetchTopologyEndpointsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date
    ) throws WebApplicationException, ProcessingException;

    @GET
    @Path("/api/v2/topology/service-types")
    WebApiServiceTypeResponse fetchServiceTypesSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date
    ) throws WebApplicationException, ProcessingException;

    @POST
    @Path("/api/v2/topology/groups")
    Status createTopologyGroupsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date,
            List<GroupTopologyDto> request
    ) throws WebApplicationException, ProcessingException;

    @POST
    @Path("/api/v2/topology/endpoints")
    Status createTopologyEndpointsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date,
            List<EndpointTopologyDto> request
    ) throws WebApplicationException, ProcessingException;

    @DELETE
    @Path("/api/v2/topology/groups")
    Status deleteTopologyGroupsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date
    ) throws WebApplicationException, ProcessingException;

    @DELETE
    @Path("/api/v2/topology/endpoints")
    Status deleteTopologyEndpointsSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date
    ) throws WebApplicationException, ProcessingException;

    @DELETE
    @Path("/api/v2/topology/service-types")
    Status deleteServiceTypesSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date
    ) throws WebApplicationException, ProcessingException;

    //    @PUT
//    @Path("/api/v2/topology/endpoints")
//    Status updateTopologyEndpointsSuperAdmin(
//            @HeaderParam("x-api-key") String apiKey,
//            @HeaderParam("x-tenant-id") String tenantId,
//            @QueryParam("date") String date,
//            List<EndpointTopologyDto> request
//    ) throws WebApplicationException, ProcessingException;
    @POST
    @Path("/api/v2/topology/service-types")
    Status createServiceTypesSuperAdmin(
            @HeaderParam("x-api-key") String apiKey,
            @HeaderParam("x-tenant-id") String tenantId,
            @QueryParam("date") String date,
            List<ServiceTypeDto> request) throws WebApplicationException,ProcessingException;

}
