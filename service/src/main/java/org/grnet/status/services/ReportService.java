package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.encrypt.EncryptRequestDto;
import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.grnet.status.dtos.report.PartialReportResponseDto;
import org.grnet.status.dtos.report.FullReportResponseDto;
import org.grnet.status.mappers.GeneralMapper;
import org.grnet.status.mappers.ReportMapper;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.utils.EncryptUtil;
import org.grnet.status.util.Utility;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.netty.util.AsciiString.contains;

@ApplicationScoped
public class ReportService {

    private static final Logger LOG = Logger.getLogger(ReportService.class);

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;
    @Inject
    EncryptUtil encryptUtil;

    @Inject
    Utility utility;

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;


    /**
     * Fetches a list of reports from the ARGO Web API.
     *
     * @param tenantId The id of the Tenant to request containing the API URL and encrypted secret.
     * @return A list of report DTOs.
     */
    public List<PartialReportResponseDto> fetchReports(String tenantId, String search) {

        LOG.info("Fetching reports from ARGO Web API...");
        var reports = argoWebApiClient.fetchReportsSuperAdmin(accessToken, tenantId);

        var partialReports = reports.data.stream()
                .filter(r -> r != null && r.info != null)
                .map(ReportMapper.INSTANCE::fullToPartialReport)
                .toList();

        if (StringUtils.isNotBlank(search)) {
            var lowerSearch = search.toLowerCase();

            partialReports = partialReports.stream()
                    .filter(r ->
                            contains(r.id.toLowerCase(), lowerSearch) ||
                                    contains(r.name.toLowerCase(), lowerSearch) ||
                                    contains(r.description.toLowerCase(), lowerSearch) ||
                                    contains(r.tenantName.toLowerCase(), lowerSearch)
                    )
                    .toList();

            partialReports = new ArrayList<>(partialReports);
        }

        return    partialReports.stream()
                        .sorted(Comparator.comparing(r -> r.disabled))
                        .collect(Collectors.toList());

    }



    /**
     * Encrypts a plain-text secret.
     *
     * @param request The request containing the plain secret.
     * @return The encrypted secret DTO.
     */
    public EncryptResponseDto encrypt(EncryptRequestDto request) {

        var encrypted = encryptUtil.encrypt(request.secret);
        return GeneralMapper.INSTANCE.toEncryptResponse(encrypted);
    }

    /**
     * Fetch tenant's report by the report id,
     * @param id, tenant's id
     * @param reportId, report's id
     * @return
     */
    public FullReportResponseDto fetchReportById(String id, String reportId) {

        var reports = argoWebApiClient.fetchReportsSuperAdmin(accessToken, id);
        boolean found = reports.data.stream()
                .anyMatch(r -> reportId.equals(r.id));

        if (!found) {
            throw new NotFoundException("Fetching Report... Not found report with id: " + reportId + " for tenant with id: " + id);
        }

        try {
            return argoWebApiClient.fetchReportByIdSuperAdmin(reportId, accessToken, id).data.get(0);
        } catch (ClientWebApplicationException e) {
            throw new ClientWebApplicationException("Fetching Report... Report not found in Argo Web Api with id: " + reportId);
        }
    }
}
