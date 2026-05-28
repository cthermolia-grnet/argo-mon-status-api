package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.argo.ArgoStatusGroupsResponse;
import org.grnet.status.dtos.status.StatusGroupResponseDto;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.mappers.StatusPageMapper;
import org.grnet.status.repositories.StatusPageRepository;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.WebApiService;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for retrieving status groups and status page configuration.
 */
@ApplicationScoped
public class StatusService {

    @Inject
    ReportService reportService;

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @Inject
    StatusPageRepository statusPageRepository;

    @Inject
    WebApiService webApiService;

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;

    /**
     * Retrieves status groups for the given tenant and report.
     *
     * @param tenantId tenant identifier
     * @param reportId report identifier
     * @return list of status groups
     */
    public List<StatusGroupResponseDto> getStatusGroups(String tenantId, String reportId) {

        //FullReportResponseDto report=null;
        webApiService.validateTenantInitialized(tenantId, "Status Groups");

        var report = reportService.fetchReportById(tenantId, reportId);

        ArgoStatusGroupsResponse argoGroups = null;
        var list = new ArrayList<StatusGroupResponseDto>();

        try {
            argoGroups = argoWebApiClient.fetchStatusGroupsSuperAdmin(accessToken, tenantId, report.info.name);
        } catch (WebApplicationException e) {
            Log.error("Argo Web Api returned HTTP error: {}", e.getResponse().getStatus(), e);
            throw new NotFoundException(
                    "Fetching Report Groups... No groups retrieved from Argo Web Api for report: " + report.info.name
            );
        } catch (ProcessingException e) {
            Log.error("Argo Web Api is unreachable", e);
            throw new RuntimeException("Fetching Report Groups... Argo Web Api is unreachable", e);
        }

        if (argoGroups != null && argoGroups.groups != null) {
            for (var group : argoGroups.groups) {
                var dto = new StatusGroupResponseDto();
                dto.name = group.name;

                if (group.statuses != null && !group.statuses.isEmpty()) {
                    dto.status = group.statuses.get(group.statuses.size() - 1).value;
                }
                list.add(dto);
            }
        }

        return list;
    }


    /**
     * Retrieves the status page configuration by slug with updated live group statuses.
     *
     * @param slug status page slug
     * @return status page configuration
     */
    @Transactional
    public StatusPageConfigDto getConfigBySlug(String slug) {

        var statusPage = statusPageRepository.find("slug", slug)
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("Status page not found for slug: " + slug));

        var statusPageDto = StatusPageMapper.INSTANCE.entityToDto(statusPage);
        var config = statusPageDto.config;

        webApiService.validateTenantInitialized(statusPage.getTenant().id, "Status Groups");

        ArgoStatusGroupsResponse argoGroups = null;
        try {
             argoGroups = argoWebApiClient.fetchStatusGroupsSuperAdmin(
                    accessToken,
                    statusPage.getTenant().id,
                    statusPage.getReport()
            );
        } catch (WebApplicationException e) {
            Log.errorf(e, "Argo Web API returned HTTP error while fetching status groups. Status: %s", e.getResponse().getStatus());
            throw new NotFoundException(
                    "Fetching Status Groups... No status groups retrieved from Argo Web API for report: " + statusPage.getReport()
            );
        } catch (ProcessingException e) {
            Log.error("Argo Web API is unreachable", e);
            throw new RuntimeException("Fetching Status Groups... Argo Web API is unreachable", e);
        }

        var liveGroups = requireGroups(argoGroups, statusPage.getReport());

        if (config.groups == null) {
            return config;
        }

        for (var group : config.groups) {
            if (group == null || group.list == null) {
                continue;
            }

            for (var item : group.list) {
                if (item == null || item.name == null) {
                    continue;
                }

                liveGroups.stream()
                        .filter(live -> live != null && item.name.equals(live.name))
                        .findFirst()
                        .ifPresent(live -> {
                            if (live.statuses != null && !live.statuses.isEmpty()) {
                                var last = live.statuses.get(live.statuses.size() - 1);
                                if (last != null && last.value != null) {
                                    item.status = last.value;
                                }
                            }
                        });
            }
        }

        return config;
    }


    private List<ArgoStatusGroupsResponse.Group> requireGroups(ArgoStatusGroupsResponse argoGroups, String report) {

        if (argoGroups == null || argoGroups.groups == null || argoGroups.groups.isEmpty()) {
            throw new NotFoundException(
                    "Fetching Status Groups... No status groups found in Argo Web API for report: " + report
            );
        }

        return argoGroups.groups;
    }

}