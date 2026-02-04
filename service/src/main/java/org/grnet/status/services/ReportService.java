package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.encrypt.EncryptRequestDto;
import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.grnet.status.dtos.report.ReportRequestDto;
import org.grnet.status.dtos.report.ReportResponseDto;
import org.grnet.status.mappers.GeneralMapper;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.utils.EncryptUtil;
import org.grnet.status.services.utils.UriUtil;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReportService {

    private static final Logger LOG = Logger.getLogger(ReportService.class);

    @Inject
    EncryptUtil encryptUtil;

    @Inject
    @RestClient
    ArgoWebApiClient argoWebApiClient;

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

}
