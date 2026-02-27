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
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.netty.util.AsciiString.contains;

/**
 * Service responsible for retrieving reports and related data from Argo Web API.
 */
@ApplicationScoped
public class ReportService {

    private static final Logger LOG = Logger.getLogger(ReportService.class);

    @ConfigProperty(name = "web.api.access.token")
    String accessToken;
    @Inject
    EncryptUtil encryptUtil;

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;


    /**
     * Retrieves a list of reports for the given tenant with optional search filtering.
     *
     * @param tenantId tenant identifier
     * @param search search filter
     * @return list of reports
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
     * Encrypts a plain text secret.
     *
     * @param request encryption request
     * @return encryption response
     */
    public EncryptResponseDto encrypt(EncryptRequestDto request) {

        var encrypted = encryptUtil.encrypt(request.secret);
        return GeneralMapper.INSTANCE.toEncryptResponse(encrypted);
    }

    /**
     * Retrieves a report by its identifier for the given tenant.
     *
     * @param id tenant identifier
     * @param reportId report identifier
     * @return report response
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
