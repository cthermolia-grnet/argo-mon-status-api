package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.profile.aggregation.AggregationProfileResponse;
import org.grnet.status.dtos.profile.metric.MetricProfileResponse;
import org.grnet.status.dtos.profile.operation.OperationProfileResponse;
import org.grnet.status.services.clients.ArgoWebApiClient;

@ApplicationScoped
public class ProfileService {

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;

    public AggregationProfileResponse listSpecificAggregationProfiles(String tenantId, String profileId, String date){

        return argoWebApiClient.listSpecificAggregationProfilesSuperAdmin(profileId, date, accessToken, tenantId);
    }

    public AggregationProfileResponse listAllAggregationProfiles(String tenantId, String date){

        return argoWebApiClient.listAllAggregationProfilesSuperAdmin(date, accessToken, tenantId);
    }

    public MetricProfileResponse listSpecificMetricProfiles(String tenantId, String profileId, String date){

        return argoWebApiClient.listSpecificMetricProfilesSuperAdmin(profileId, date, accessToken, tenantId);
    }

    public MetricProfileResponse listAllMetricProfiles(String tenantId, String date){

        return argoWebApiClient.listAllMetricProfilesSuperAdmin(date, accessToken, tenantId);
    }

    public OperationProfileResponse listSpecificOperationsProfiles(String tenantId, String profileId, String date){

        return argoWebApiClient.listSpecificOperationsProfilesSuperAdmin(profileId, date, accessToken, tenantId);
    }

    public OperationProfileResponse listAllOperationsProfiles(String tenantId, String date){

        return argoWebApiClient.listAllOperationsProfilesSuperAdmin(date, accessToken, tenantId);
    }
}
