package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.status.EventStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.enums.EventStatus;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
//@TestHTTPEndpoint(AdminEndpoint.class)
public class AutomationEndpointTest extends KeycloakTest {
    @InjectMock
    ArgoWebApiClientFactory argoWebApiClientFactory;

    private String currentMockId;

    @BeforeEach
    public void mockArgoClient() throws Exception {
        var mockClient = org.mockito.Mockito.mock(ArgoWebApiClient.class);

        when(mockClient.createTenant(any(), any())).thenAnswer(invocation -> {
            // Use the currentMockId set by the test
            return loadMockTenantResponse(currentMockId);
        });

        when(mockClient.getTenant(any(), any())).thenAnswer(invocation -> {
            // Use the currentMockId set by the test
            return loadMockTenantGetResponse(currentMockId);
        });
        when(argoWebApiClientFactory.buildClient(anyString())).thenReturn(mockClient);
    }


    @BeforeEach
    public void cleanUp() {
        tenantService.deleteAll();
    }

    @Test
    public void updateTenantStatus() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "TENANT TEST";
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";
        request.info = tenantInfo;

        var created = given()
                .auth().oauth2(adminToken)
                .basePath("/v1/admin")//
                .contentType(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var statusReq = new TenantStatusDto();
        statusReq.jobs = new ArrayList<>();

        var job = new EventStatusDto();
        job.name = "init_ams";
        job.status = EventStatus.COMPLETED.name().toLowerCase(); // "completed"
        job.message = "Creating indexes in mongo";
        job.start = Instant.parse("2025-10-22T12:44:48.107Z");
        job.end = Instant.parse("2025-10-22T12:44:48.107Z");
        statusReq.jobs.add(job);

        TenantStatusDto updated = given()
                .auth().oauth2(automationToken)
                .contentType(ContentType.JSON)
                .body(statusReq)
                .when()
                .put("/v1/automation/tenant/{id}/status", created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantStatusDto.class);

        assertEquals(1, updated.jobs.size());
        assertEquals("init_ams", updated.jobs.get(0).name);
        assertEquals(EventStatus.COMPLETED.name().toLowerCase(), updated.jobs.get(0).status);
    }


    private TenantWebApiCreateResponse loadMockTenantGetResponse(String id) {

        var tenantWebApiResponse = new TenantWebApiCreateResponse();
        var data = new TenantWebApiCreateResponse.Data();
        var link = new TenantWebApiCreateResponse.Links();
        var status = new TenantWebApiCreateResponse.Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        link.setSelf("https://https://test.api.grnet.gr/api/v2/admin/tenants/e1ab046c-8544-47e6-bd8f-e8aa8b83acb3");
        data.setId(id);
        data.setLinks(link);
        tenantWebApiResponse.setData(data);
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }

    private TenantWebApiCreateResponse loadMockTenantResponse(String id) {

        var tenantWebApiResponse = new TenantWebApiCreateResponse();
        var data = new TenantWebApiCreateResponse.Data();
        var link = new TenantWebApiCreateResponse.Links();
        var status = new TenantWebApiCreateResponse.Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        link.setSelf("https://https://test.api.grnet.gr/api/v2/admin/tenants/e1ab046c-8544-47e6-bd8f-e8aa8b83acb3");
        data.setId(id);
        data.setLinks(link);
        tenantWebApiResponse.setData(data);
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }

}
