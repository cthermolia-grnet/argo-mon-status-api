package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.grnet.status.api.endpoints.AdminEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.project.ProjectUpdateDto;
import org.grnet.status.dtos.tenant.*;
import org.grnet.status.dtos.tenantproject.TenantProjectDeleteDto;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(AdminEndpoint.class)
public class AdminEndpointTest extends KeycloakTest {

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
    @Test
    public void superAdminCanFetchAllPages() {
        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/pages?page=1&size=5")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(PageResource.class);
    }

    @Test
    public void normalUserCannotFetchAllPages() {
        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/pages?page=1&size=5")
                .then()
                .assertThat()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Access denied — super admin privileges required.", error.message);
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

    private TenantWebApiGetResponse loadMockTenantGetResponse(String id) {

        var tenantWebApiResponse = new TenantWebApiGetResponse();
        tenantWebApiResponse.setData(new ArrayList<>());
        var data = new TenantWebApiGetResponse.Data();
        data.setId(id);
        var info = new TenantWebApiGetResponse.Info();
        info.setCreated("2025-01-01 00:00:00");
        info.setName("TENANT TEST");
        info.setEmail("test@gmail.com");
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
    public void createTenant() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "TENANT TEST";
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";
        request.info = tenantInfo;

        var api = "https://test.api.grnet.gr";
        var secret = "VaWi0ZBjGrxXPuB0o+KARpH63EKDaiwttfLE54POPtaw4QRxYktsabA+CT76sX0D";
        ;

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)

                .queryParam("api", api)
                .queryParam("secret", secret)
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);
        assertEquals(request.info.name, response.info.name);
        assertEquals(currentMockId, response.id);  // check that the id matches what you set
    }

    @Test
    public void getTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "TENANT TEST";
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
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var getTenant = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/tenants/{id}", response.id)
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
        tenantInfo.name = "TENANT TEST";
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
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        tenantInfo1.name = "TENANT TEST UPDATED";
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
                .put("/tenants/{id}", response.id)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        assertEquals(tenantInfo1.name, response1.info.name);
    }

    @Test
    public void updateTenantForbiddenUser() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "TENANT TEST";
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
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        tenantInfo1.name = "TENANT TEST UPDATED" ;
        tenantInfo1.email = "test2-updated@gmail.com";
        tenantInfo1.description = "this is test2 updated tenant description";
        tenantInfo1.image = "https://example/image.png";
        tenantInfo1.website = "https://test2.updated.tenant.org";
        request1.info = tenantInfo1;
        var response1 = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request1)
                .contentType(ContentType.JSON)
                .when()
                .put("/tenants/{id}", response.id)
                .then()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Access denied — super admin privileges required.", response1.message);
    }

    @Test
    public void updateNotExistingTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        tenantInfo1.name = "TENANT TEST UPDATED";
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
                .put("/tenants/{id}", currentMockId)
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("There is no Tenant with the following id:  " + currentMockId, response1.message);
    }

    @Test
    public void deleteTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "TENANT TEST";
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
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var response1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", response.id)
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, response1.code);
    }


    @Test
    public void deleteTenantForbidden() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = "TENANT TEST";
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
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", response.id)
                .then()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(403, error.code);
        assertEquals("Access denied — super admin privileges required.", error.message);

    }

    @Test
    public void deleteTenantNotExisting() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var error = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", currentMockId)
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);
        assertEquals(404, error.code);

    }
    @Test
    public void notExistingTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        var error = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/tenants/{id}", currentMockId)
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);

    }
    @Test
    public void testAssignMultipleProjects() {
        currentMockId = "tenant-bbb-ccc";

        // Create tenant
        var tenantReq = new TenantRequestDto();
        var tinfo = new TenantInfoDto();
        tinfo.name = "TENANTASSIGNPROJECTS";
        tinfo.email = "tx@example.com";
        tinfo.description = "This is a description";
        tenantReq.info = tinfo;

        var tenant = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(tenantReq)
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        // Create two projects
        var p1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(buildCreateRequest())
                .post("/projects")
                .then()
                .statusCode(201)
                .extract().as(ProjectResponseDto.class);

        var p2 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(buildCreateRequest())
                .post("/projects")
                .then()
                .statusCode(201)
                .extract().as(ProjectResponseDto.class);

        var req = new TenantProjectRequestDto();
        req.tenantId = tenant.id;
        req.projectIds = List.of(p1.id, p2.id);

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .put("/tenant-project")
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, response.code);
    }

    @Test
    public void testGetProjectsByTenant() {
        currentMockId = "tenant-xyz";

        var tenantReq = new TenantRequestDto();
        var info = new TenantInfoDto();
        info.name = "TENANTGETPROJECT";
        info.email = "fetch@example.com";
        info.description = "This is a description";
        tenantReq.info = info;

        var tenant = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(tenantReq)
                .post("/tenants")
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
                .post("/projects")
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
                .put("/tenant-project")
                .then()
                .statusCode(200);


        var result = given()
                .auth()
                .oauth2(adminToken)
                .get("/tenants/" + tenant.id + "/projects")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
    }


    @Test
    public void testDeleteTenantProjectAssignment() {
        currentMockId = "tenant-del";

        var tenantReq = new TenantRequestDto();
        var tInfo = new TenantInfoDto();
        tInfo.name = "TENANTDELETE";
        tInfo.email = "td@example.com";
        tInfo.description = "This is a description for testing";
        tenantReq.info = tInfo;

        var tenant = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(tenantReq)
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        var project = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(buildCreateRequest())
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var assignReq = new TenantProjectRequestDto();
        assignReq.tenantId = tenant.id;
        assignReq.projectIds = List.of(project.id);

        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(assignReq)
                .put("/tenant-project")
                .then()
                .statusCode(200);

        var deleteReq = new TenantProjectDeleteDto();
        deleteReq.projectId = project.id;
        deleteReq.tenantId = tenant.id;

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(deleteReq)
                .delete("/tenant-project")
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(204, response.code);

        var after = given()
                .auth().oauth2(adminToken)
                .get("/tenants/" + tenant.id + "/projects")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertEquals(0, after.getContent().size());
    }


    @Test
    public void testDeleteAssignmentNotExisting() {
        var deleteReq = new TenantProjectDeleteDto();
        deleteReq.tenantId = "nonexistent-tenant";
        deleteReq.projectId = "nonexistent-project";

        var result = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(deleteReq)
                .delete("/tenant-project")
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(404, result.code);
    }

    @Test
    public void testAssignInvalidTenant() {
        var req = new TenantProjectRequestDto();
        req.tenantId = "tenant-not-exist";
        req.projectIds = List.of("proj-not-exist");

        var error = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .put("/tenant-project")
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(404, error.code);
    }





    @Test
    public void testCreateProject() {
        var req = buildCreateRequest();

        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        assertNotNull(created.id);
    }

    @Test
    public void testGetProject() {
        var req = buildCreateRequest();

        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var fetched = given()
                .auth().oauth2(adminToken)
                .get("/projects/" + created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(ProjectResponseDto.class);

        assertEquals(created.id, fetched.id);
        assertEquals(created.name, fetched.name);
    }

    @Test
    public void testUpdateProject() {
        var req = buildCreateRequest();

        var created = given()
                .auth()
                .oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var update = new ProjectUpdateDto();
        update.name = "UPDATED NAME " + UUID.randomUUID();
        update.dataRetentionPolicy = "Retention policy text";


        var updated = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/projects/" + created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(ProjectResponseDto.class);

        assertTrue(updated.name.contains("UPDATED NAME"));
        assertEquals("Retention policy text", updated.dataRetentionPolicy);
    }

    @Test
    public void testDeleteProject() {
        var req = buildCreateRequest();

        var created = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(req)
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .as(ProjectResponseDto.class);

        var response = given()
                .auth().oauth2(adminToken)
                .delete("/projects/" + created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, response.code);
        assertEquals("Project has been successfully deleted.", response.message);
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
//
//    @Test
//    public void getTenants() {
//
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
//
//        var request = new TenantRequestDto();
//        var tenantInfo = new TenantInfoDto();
//        tenantInfo.name = "Tenant Test1";
//        tenantInfo.email = "test@gmail.com";
//        tenantInfo.description = "this is test tenant description";
//        tenantInfo.image = "/images/logo.png";
//        tenantInfo.website = "https://test.tenant.org";
//        request.info = tenantInfo;
//
//        //var webApi = new ArgoWebApiRequest();
//
//        var response = given()
//                .auth().oauth2(adminToken)
//                .contentType(ContentType.JSON)
//                .body(request)
//                .contentType(ContentType.JSON)
//                .when()
//                .post("/tenants")
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(TenantResponseDto.class);
//
//        currentMockId = "11ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
//
//        var request1 = new TenantRequestDto();
//        var tenantInfo1 = new TenantInfoDto();
//        tenantInfo1.name = "Tenant Test3";
//        tenantInfo1.email = "test3@gmail.com";
//        tenantInfo1.description = "this is test tenant description";
//        tenantInfo1.image = "/images/logo.png";
//        tenantInfo1.website = "https://test.tenant.org";
//        request1.info = tenantInfo1;
//
//        //var webApi = new ArgoWebApiRequest();
//
//        var response1 = given()
//                .auth().oauth2(adminToken)
//                .contentType(ContentType.JSON)
//                .body(request1)
//                .contentType(ContentType.JSON)
//                .when()
//                .post("/tenants")
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(TenantResponseDto.class);
//
//
//        var getresponse = given()
//                .auth().oauth2(adminToken)
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/tenants")
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .extract()
//                .as(TenantResponseDto[].class);
//
//        assertEquals(2,getresponse.length);
//    }
//
//
//    @Test
//    public void getTenantsByName() {
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
//
//        var request = new TenantRequestDto();
//        var tenantInfo = new TenantInfoDto();
//        tenantInfo.name = "Tenant Test1";
//        tenantInfo.email = "test@gmail.com";
//        tenantInfo.description = "this is test tenant description";
//        tenantInfo.image = "/images/logo.png";
//        tenantInfo.website = "https://test.tenant.org";
//        request.info = tenantInfo;
//
//        //var webApi = new ArgoWebApiRequest();
//
//        var response = given()
//                .auth().oauth2(adminToken)
//                .contentType(ContentType.JSON)
//                .body(request)
//                .contentType(ContentType.JSON)
//                .when()
//                .post("/tenants")
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(TenantResponseDto.class);
//
//        currentMockId = "11ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
//
//
//        var request1 = new TenantRequestDto();
//        var tenantInfo1 = new TenantInfoDto();
//        tenantInfo1.name = "Tenant Test2";
//        tenantInfo1.email = "test@gmail.com";
//        tenantInfo1.description = "this is test tenant description";
//        tenantInfo1.image = "/images/logo.png";
//        tenantInfo1.website = "https://test.tenant.org";
//        request1.info = tenantInfo1;
//
//        //var webApi = new ArgoWebApiRequest();
//
//        var response1 = given()
//                .auth().oauth2(adminToken)
//                .contentType(ContentType.JSON)
//                .body(request1)
//                .contentType(ContentType.JSON)
//                .when()
//                .post("/tenants")
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(TenantResponseDto.class);
//
//
//        var getresponse = given()
//                .auth().oauth2(adminToken)
//                .contentType(ContentType.JSON)
//                .queryParam("name","Test Tenant2")
//                .when()
//                .get("/tenants")
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .extract()
//                .as(TenantResponseDto[].class);
//
//        assertEquals(1,getresponse.length);
//       //assertEquals("Test Tenant2",getresponse[0].info.name);
//    }


}