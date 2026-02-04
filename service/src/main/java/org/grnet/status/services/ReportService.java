package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.encrypt.EncryptRequestDto;
import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.grnet.status.dtos.report.ReportRequestDto;
import org.grnet.status.dtos.report.ReportResponseDto;
import org.grnet.status.dtos.report.FullReportResponseDto;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.mappers.GeneralMapper;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.utils.EncryptUtil;
import org.grnet.status.services.utils.TenantUtil;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.ArrayList;
import java.util.List;

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

    @Inject
    TenantUtil tenantUtil;

    /**
     * Fetches a list of reports from the ARGO Web API.
     *
     * @param request The report request containing the API URL and encrypted secret.
     * @return A list of report DTOs.
     */
    public List<ReportResponseDto> fetchReports(ReportRequestDto request) {
        var decryptedSecret = encryptUtil.decrypt(request.secret);

        LOG.info("Building ARGO Web API client...");

        var response = argoWebApiClient.fetchReports(decryptedSecret);

        var list = new ArrayList<ReportResponseDto>();
        if (response != null && response.data != null) {
            for (var item : response.data) {
                if (item.info != null) {
                    var dto = new ReportResponseDto();
                    dto.name = item.info.name;
                    dto.description = item.info.description;
                    list.add(dto);
                }
            }
        }
        return list;
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

        String apiKey = tenantUtil.getArgoEngineKey(accessToken, id);

        if (apiKey == null) {
            throw new NotFoundException("Not found argo-engine token for tenant with id: " + id);
        }
        var reports = argoWebApiClient.fetchReports(apiKey);
        boolean found = reports.data.stream()
                .anyMatch(r -> reportId.equals(r.id));

        if (!found) {
            throw new NotFoundException("Not found report with id: " + reportId + " for tenant with id: " + id);
        }

        try {
            return argoWebApiClient.fetchReportById(id, apiKey).data.get(0);
        } catch (ClientWebApplicationException e) {
            throw new ClientWebApplicationException("Report not found in argo-web-api with id: " + reportId);
        }
    }

}
