package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.status.StatusGroupRequestDto;
import org.grnet.status.dtos.status.StatusGroupResponseDto;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.mappers.StatusPageMapper;
import org.grnet.status.repositories.StatusPageRepository;
import org.grnet.status.services.clients.ArgoWebApiClient;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class StatusService {

    @Inject
    ReportService reportService;

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @Inject
    StatusPageRepository statusPageRepository;

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;

    public List<StatusGroupResponseDto> getStatusGroups(String tenantId, String reportId) {

        var report = reportService.fetchReportById(tenantId, reportId);
        var argoGroups = argoWebApiClient.fetchStatusGroupsSuperAdmin(accessToken, tenantId, report.info.name);

        var list = new ArrayList<StatusGroupResponseDto>();

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


    @Transactional
    public StatusPageConfigDto getConfigBySlug(String slug) {
        var statusPage = statusPageRepository.find("slug", slug)
                .firstResultOptional()
                .orElseThrow(() -> new IllegalArgumentException("Status page not found for slug: " + slug));

        // Convert entity → DTO (config already deserialized)
        var statusPageDto = StatusPageMapper.INSTANCE.entityToDto(statusPage);
        var config = statusPageDto.config;

        // Fetch live groups
        var argoGroups = argoWebApiClient.fetchStatusGroupsSuperAdmin(accessToken, statusPage.getTenant().id,statusPage.getReport());

        // Update config group statuses in memory
        for (var group : config.groups) {
            if (group == null || group.list == null) continue;

            for (var item : group.list) {
                if (item == null || item.name == null) continue;

                argoGroups.groups.stream()
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


}