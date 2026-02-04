package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.api.endpoints.ReportEndpoint;
import org.grnet.status.dtos.argo.ArgoReportsResponse;
import org.grnet.status.dtos.encrypt.EncryptRequestDto;
import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.grnet.status.dtos.report.ReportRequestDto;
import org.grnet.status.dtos.report.ReportResponseDto;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ReportEndpoint.class)
public class ReportEndpointTest extends KeycloakTest {

    @InjectMock
    @RestClient
    ArgoWebApiClient argoWebApiClient;
    @ConfigProperty(name = "web.api.url")
    String webapi;
    @ConfigProperty(name = "web.api.access.token")
    String webApiToken;

    @BeforeEach
    public void mockArgoClient() throws Exception {

        var mockResponse = loadMockReports();
        when(argoWebApiClient.fetchReports(any())).thenReturn(mockResponse);
    }

    @Test
    public void fetchReports() {

        var encryptRequest = new EncryptRequestDto();
        encryptRequest.secret = webApiToken;

        var encryptedKey = given()
                .auth()
                .oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(encryptRequest)
                .when()
                .post("/encrypt")
                .then()
                .statusCode(200)
                .extract()
                .as(EncryptResponseDto.class);

        var reportRequest = new ReportRequestDto();
        reportRequest.api = "https://api.test.gr";
        reportRequest.secret = encryptedKey.secret;

        given()
                .auth()
                .oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(reportRequest)
                .when()
                .post("/reports")
                .then()
                .statusCode(200)
                .extract()
                .as(ReportResponseDto[].class);
    }

    private ArgoReportsResponse loadMockReports() throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mocks/reports.json")) {
            return new ObjectMapper().readValue(is, ArgoReportsResponse.class);
        }
    }
}
