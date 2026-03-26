package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.topology.EndpointTopologyDto;
import org.grnet.status.dtos.topology.GroupTopologyDto;
import org.grnet.status.dtos.topology.ServiceTypeDto;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.WebApiService;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class TopologyService {
    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @Inject
    WebApiService webApiService;

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;

    private static final Logger LOG = Logger.getLogger(TopologyService.class);

    public List<GroupTopologyDto> fetchGroupTopologies(String id, String date) {

        LOG.info("Fetching group topology from ARGO Web API...");
        webApiService.validateTenantInitialized(id, "Group Topology");
        var response = argoWebApiClient.fetchTopologyGroupsSuperAdmin(accessToken, id, date);
        return response.data;
    }

    public List<EndpointTopologyDto> fetchEndpointTopologies(String id, String date) {

        LOG.info("Fetching endpoint topology from ARGO Web API...");
        webApiService.validateTenantInitialized(id, "Endpoint Topology");
        var response = argoWebApiClient.fetchTopologyEndpointsSuperAdmin(accessToken, id, date);
        return response.data;
    }

    public List<ServiceTypeDto> fetchServiceTypes(String id, String date) {

        LOG.info("Fetching service types from ARGO Web API...");
        webApiService.validateTenantInitialized(id, "Service Type");
        var response = argoWebApiClient.fetchServiceTypesSuperAdmin(accessToken, id, date);
        return response.data;
    }

    public Status createGroupTopology(String id, String date, Boolean force, List<GroupTopologyDto> request) {

        LOG.info("Creating group topology to ARGO Web API...");
        try {


            webApiService.validateTenantInitialized(id, "Group Topology");

            force = Boolean.TRUE.equals(force) ? Boolean.TRUE : null;
            return argoWebApiClient.createTopologyGroupsSuperAdmin(accessToken, id, date, force, request);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status createEndpointTopology(String id, String date, Boolean force, List<EndpointTopologyDto> request) {

        LOG.info("Creating endpoint topology to ARGO Web API...");

        try {
            // Validate tenant and create topology
            webApiService.validateTenantInitialized(id, "Endpoint Topology");

            force = Boolean.TRUE.equals(force) ? Boolean.TRUE : null;
            return argoWebApiClient.createTopologyEndpointsSuperAdmin(accessToken, id, date, force, request);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }


    public Status deleteGroupTopology(String id, String date) {

        LOG.info("Deleting group topology from ARGO Web API...");
        try {
            webApiService.validateTenantInitialized(id, "Group Topology");
            return argoWebApiClient.deleteTopologyGroupsSuperAdmin(accessToken, id, date);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status deleteEndpointTopology(String id, String date) {

        LOG.info("Deleting endpoint topology from ARGO Web API...");
        try {
            webApiService.validateTenantInitialized(id, "Endpoint Topology");
            return argoWebApiClient.deleteTopologyEndpointsSuperAdmin(accessToken, id, date);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status deleteServiceTypes(String id, String date) {

        LOG.info("Deleting service types from ARGO Web API...");
        try {
            webApiService.validateTenantInitialized(id, "Service Type");
            return argoWebApiClient.deleteServiceTypesSuperAdmin(accessToken, id, date);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

    public Status createServiceTypes(String id, String date, Boolean force, List<ServiceTypeDto> request) {

        LOG.info("Creating service types to ARGO Web API...");

        try {
            webApiService.validateTenantInitialized(id, "Service Type");

            force = Boolean.TRUE.equals(force) ? Boolean.TRUE : null;
            return argoWebApiClient.createServiceTypesSuperAdmin(accessToken, id, date, force, request);

        } catch (ProcessingException e) {
            e.printStackTrace();
            System.out.println("CAUSE: " + e.getCause());
            throw e;
        }
    }

}
