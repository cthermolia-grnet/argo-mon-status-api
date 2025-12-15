package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.grnet.status.api.endpoints.ReportEndpoint;
import org.grnet.status.dtos.argo.ArgoReportsResponse;
import org.grnet.status.dtos.encrypt.EncryptRequestDto;
import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.grnet.status.dtos.report.ReportRequestDto;
import org.grnet.status.dtos.report.ReportResponseDto;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(ReportEndpoint.class)
public class ReportEndpointTest extends KeycloakTest{

    @InjectMock
    ArgoWebApiClientFactory factory;

    @BeforeEach
    public void mockArgoClient() throws Exception {

        var mockClient = org.mockito.Mockito.mock(ArgoWebApiClient.class);
        var mockResponse = loadMockReports();
        when(mockClient.fetchReports(any())).thenReturn(mockResponse);
        when(factory.buildClient(anyString())).thenReturn(mockClient);
    }

    @Test
    public void fetchReports() {

        var encryptRequest = new EncryptRequestDto();
        encryptRequest.secret = "access_token";

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
        reportRequest.api = "https://api.devel.mon.argo.grnet.gr";
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
