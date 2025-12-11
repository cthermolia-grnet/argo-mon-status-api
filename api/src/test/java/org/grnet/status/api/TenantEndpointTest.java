package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.grnet.status.api.endpoints.TenantEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.tenant.*;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
//@TestHTTPEndpoint(TenantEndpoint.class)
public class TenantEndpointTest extends KeycloakTest {

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


    // -------------------------------------------------------------
    // ENTITLEMENTS TESTING
    // -------------------------------------------------------------

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

    private TenantWebApiGetResponse loadMockTenantGetResponse(String id) {

        var tenantWebApiResponse = new TenantWebApiGetResponse();
        tenantWebApiResponse.setData(new ArrayList<>());
        var data = new TenantWebApiGetResponse.Data();
        data.setId(id);
        var info = new TenantWebApiGetResponse.Info();
        info.setCreated("2025-01-01 00:00:00");
        info.setName("LOCALTENANT");
        info.setEmail("localtenanttest@gmail.com");
        info.setDescription("this is test tenant description");
        info.setImage("https://example/image.png");
        info.setWebsite("https://test.tenant.org");
        info.setUpdated("2025-01-02 00:00:00");
        data.setInfo(info);
        tenantWebApiResponse.getData().add(data);
        data.setInfo(info);
        var status = new TenantWebApiGetResponse.Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }
    @Test
    public void getTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "LOCALTENANT";
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";
        request.info = tenantInfo;

        //var webApi = new ArgoWebApiRequest();

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var getTenant = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}", response.id)
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        assertEquals(tenantInfo.name, getTenant.info.name);
    }

    @Test
    public void updateTenant() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "LOCALTENANT";
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";
        request.info = tenantInfo;

        //var webApi = new ArgoWebApiRequest();

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        tenantInfo1.name = "LOCALTENANT";
        tenantInfo1.email = "test2-updated@gmail.com";
        tenantInfo1.description = "this is test2 updated tenant description";
        tenantInfo1.image = "https://example/image.png";
        tenantInfo1.website = "https://test2.updated.tenant.org";
        request1.info = tenantInfo1;
        var response1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request1)
                .contentType(ContentType.JSON)
                .when()
                .put("/v1/tenants/{id}", response.id)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        assertEquals("LOCALTENANT", response1.info.name);
    }

    @Test
    public void updateTenantForbiddenUser() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "LOCALTENANT";
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";
        request.info = tenantInfo;

        //var webApi = new ArgoWebApiRequest();

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        tenantInfo.name = "LOCALTENANT";
        tenantInfo1.email = "test2-updated@gmail.com";
        tenantInfo1.description = "this is test2 updated tenant description";
        tenantInfo1.image = "https://example/image.png";
        tenantInfo1.website = "https://test2.updated.tenant.org";
        request1.info = tenantInfo1;
        var response1 = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request1)
                .contentType(ContentType.JSON)
                .when()
                .put("/v1/tenants/{id}", response.id)
                .then()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Access denied — group='tenants', role='admin', id='" + response.id+ "'", response1.message);
    }

    @Test
    public void testGetProjectsByTenant() {
        currentMockId = "tenant-xyz";

        var tenantReq = new TenantRequestDto();
        var info = new TenantInfoDto();
        info.name = "LOCALTENANT";
        info.email = "fetch@example.com";
        info.description = "This is a description";
        tenantReq.info = info;

        var tenant = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(tenantReq)
                .post("/v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var projectReq = buildCreateRequest();
        var project = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(projectReq)
                .post("/v1/admin/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var assignReq = new TenantProjectRequestDto();
        assignReq.tenantId = tenant.id;
        assignReq.projectIds = List.of(project.id);

        given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(assignReq)
                .put("/v1/admin/tenant-project")
                .then()
                .statusCode(200);


        var result = given()
                .auth()
                .oauth2(adminToken)
                .get("/v1/tenants/" + tenant.id + "/projects")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
    }


    private ProjectRequestDto buildCreateRequest() {

        var dto = new ProjectRequestDto();
        dto.name = "Test Project" + UUID.randomUUID();
        dto.startDate = Timestamp.from(Instant.now());
        dto.endDate = Timestamp.from(Instant.now());
        dto.sustainabilityEndDate = Timestamp.from(Instant.now());
        dto.dataRetentionPolicy = "Retention policy text";

        return dto;
    }
}