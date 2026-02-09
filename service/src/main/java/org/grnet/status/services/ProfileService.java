package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.profile.aggregation.AggregationProfileResponse;
import org.grnet.status.dtos.profile.metric.MetricProfileResponse;
import org.grnet.status.dtos.profile.operation.OperationProfileResponse;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.utils.TenantUtil;

@ApplicationScoped
public class ProfileService {

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @Inject
    TenantUtil tenantUtil;

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;

    public AggregationProfileResponse listSpecificAggregationProfiles(String tenantId, String profileId, String date){


        String apiKey = tenantUtil.getArgoEngineKey(accessToken, tenantId);
        return argoWebApiClient.listSpecificAggregationProfiles(profileId, date, apiKey);
    }

    public AggregationProfileResponse listAllAggregationProfiles(String tenantId, String date){

        String apiKey = tenantUtil.getArgoEngineKey(accessToken, tenantId);
        return argoWebApiClient.listAllAggregationProfiles(date, apiKey);
    }

    public MetricProfileResponse listSpecificMetricProfiles(String tenantId, String profileId, String date){

        String apiKey = tenantUtil.getArgoEngineKey(accessToken, tenantId);
        return argoWebApiClient.listSpecificMetricProfiles(profileId, date, apiKey);
    }

    public MetricProfileResponse listAllMetricProfiles(String tenantId, String date){

        String apiKey = tenantUtil.getArgoEngineKey(accessToken, tenantId);
        return argoWebApiClient.listAllMetricProfiles(date, apiKey);
    }

    public OperationProfileResponse listSpecificOperationsProfiles(String tenantId, String profileId, String date){

        String apiKey = tenantUtil.getArgoEngineKey(accessToken, tenantId);
        return argoWebApiClient.listSpecificOperationsProfiles(profileId, date, apiKey);
    }

    public OperationProfileResponse listAllOperationsProfiles(String tenantId, String date){

        String apiKey = tenantUtil.getArgoEngineKey(accessToken, tenantId);
        return argoWebApiClient.listAllOperationsProfiles(date, apiKey);
    }
}
