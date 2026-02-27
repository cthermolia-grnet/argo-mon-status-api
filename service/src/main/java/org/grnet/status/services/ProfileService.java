package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.profile.aggregation.AggregationProfileResponse;
import org.grnet.status.dtos.profile.metric.MetricProfileResponse;
import org.grnet.status.dtos.profile.operation.OperationProfileResponse;
import org.grnet.status.services.clients.ArgoWebApiClient;

/**
 * Service responsible for retrieving aggregation, metric, and operation profiles from Argo Web API.
 */
@ApplicationScoped
public class ProfileService {

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;

    /**
     * Retrieves a specific aggregation profile for the given tenant and date.
     *
     * @param tenantId tenant identifier
     * @param profileId aggregation profile identifier
     * @param date reference date
     * @return aggregation profile response
     */
    public AggregationProfileResponse listSpecificAggregationProfiles(String tenantId, String profileId, String date){

        return argoWebApiClient.listSpecificAggregationProfilesSuperAdmin(profileId, date, accessToken, tenantId);
    }

    /**
     * Retrieves all aggregation profiles for the given tenant and date.
     *
     * @param tenantId tenant identifier
     * @param date reference date
     * @return aggregation profiles response
     */
    public AggregationProfileResponse listAllAggregationProfiles(String tenantId, String date){

        return argoWebApiClient.listAllAggregationProfilesSuperAdmin(date, accessToken, tenantId);
    }

    /**
     * Retrieves a specific metric profile for the given tenant and date.
     *
     * @param tenantId tenant identifier
     * @param profileId metric profile identifier
     * @param date reference date
     * @return metric profile response
     */
    public MetricProfileResponse listSpecificMetricProfiles(String tenantId, String profileId, String date){

        return argoWebApiClient.listSpecificMetricProfilesSuperAdmin(profileId, date, accessToken, tenantId);
    }

    /**
     * Retrieves all metric profiles for the given tenant and date.
     *
     * @param tenantId tenant identifier
     * @param date reference date
     * @return metric profiles response
     */
    public MetricProfileResponse listAllMetricProfiles(String tenantId, String date){

        return argoWebApiClient.listAllMetricProfilesSuperAdmin(date, accessToken, tenantId);
    }

    /**
     * Retrieves a specific operation profile for the given tenant and date.
     *
     * @param tenantId tenant identifier
     * @param profileId operation profile identifier
     * @param date reference date
     * @return operation profile response
     */
    public OperationProfileResponse listSpecificOperationsProfiles(String tenantId, String profileId, String date){

        return argoWebApiClient.listSpecificOperationsProfilesSuperAdmin(profileId, date, accessToken, tenantId);
    }

    /**
     * Retrieves all operation profiles for the given tenant and date.
     *
     * @param tenantId tenant identifier
     * @param date reference date
     * @return operation profiles response
     */
    public OperationProfileResponse listAllOperationsProfiles(String tenantId, String date){

        return argoWebApiClient.listAllOperationsProfilesSuperAdmin(date, accessToken, tenantId);
    }
}
