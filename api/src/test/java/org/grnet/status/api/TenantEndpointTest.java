package org.grnet.status.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.dtos.ams.PublishResponse;
import org.grnet.status.dtos.argo.ArgoStatusGroupsResponse;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.report.PartialReportResponseDto;
import org.grnet.status.dtos.report.WebApiReportResponse;
import org.grnet.status.dtos.status.StatusGroupResponseDto;
import org.grnet.status.dtos.statuspage.*;
import org.grnet.status.dtos.tenant.*;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenantproject.TenantProjectDeleteDto;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.services.clients.AmsClient;
import org.grnet.status.services.clients.AmsClientFactory;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class TenantEndpointTest extends KeycloakTest {

    @InjectMock
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    @InjectMock
    AmsClientFactory amsClientFactory;

    private String currentMockId;

    @BeforeEach
    public void mockArgoClient() throws Exception {

        when(argoWebApiClient.createTenant(any(), any())).thenAnswer(invocation -> {
            // Use the currentMockId set by the test
            return loadMockTenantResponse(currentMockId);
        });

        when(argoWebApiClient.getTenant(any(), any())).thenAnswer(invocation -> {
            // Use the currentMockId set by the test
            return loadMockTenantGetResponse(currentMockId);
        });
        var mockOneReportResponse = loadMockReport();
        when(argoWebApiClient.fetchReportByIdSuperAdmin(any(), any(), any())).thenReturn(mockOneReportResponse);
        var mockResponse = loadMockReports();
        when(argoWebApiClient.fetchReportsSuperAdmin(any(), any())).thenReturn(mockResponse);

        when(argoWebApiClient.fetchStatusGroupsSuperAdmin(any(), any(), any())).thenReturn(loadMockStatusGroups());

    }


    @BeforeEach
    void mockAmsClient() {
        AmsClient mockClient = mock(AmsClient.class);

        when(amsClientFactory.buildClient(anyString()))
                .thenReturn(mockClient);

        var resp = new PublishResponse();
        resp.setMessageIds(List.of("mock-message-id-1"));

        when(mockClient.publish(anyString(), anyString(), anyString(), any(PublishRequest.class)))
                .thenReturn(resp);
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
        var status = new Status();
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

        var argoEngineUser = new TenantWebApiGetResponse.User();
        argoEngineUser.setName("argo_engine_MOCK-TENANT");
        argoEngineUser.setEmail("devlists.grnet.gr");
        argoEngineUser.setId("85abe3bb-ecfb-4442-9f62-30d023ce08c3");
        argoEngineUser.setApi_key("5e2f401f4226321f5c2ccba0c7b509172273481f0ec36ee22e8e910875a9b239");
        var roles = new ArrayList<String>();
        roles.add("admin");
        argoEngineUser.setRoles(roles);

        data.setUsers(List.of(argoEngineUser));

        tenantWebApiResponse.getData().add(data);
        data.setInfo(info);
        var status = new Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }

    @Test
    public void getTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var tenant = createTenant("LOCALTENANT");

        //var webApi = new ArgoWebApiRequest();

        var getTenant = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}", tenant.id)
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        assertEquals(tenant.info.name, getTenant.info.name);
    }

    @Test
    public void updateTenant() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = createTenant("LOCALTENANT");

        //var webApi = new ArgoWebApiRequest();

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        var tenantContact1 = new ContactDto();

        tenantInfo1.name = "LOCALTENANT";
        tenantInfo1.email = "test2-updated@gmail.com";
        tenantInfo1.description = "this is test2 updated tenant description";
        tenantInfo1.image = "https://example/image.png";
        tenantInfo1.website = "https://test2.updated.tenant.org";

        tenantContact1.name = "Test User";
        tenantContact1.email = "test@gmail.com";
        tenantContact1.type = "ADMIN";

        request1.info = tenantInfo1;
        request1.contacts = Collections.singletonList(tenantContact1);

        var response1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request1)
                .contentType(ContentType.JSON)
                .when()
                .put("/v1/tenants/{id}", request.id)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);

        assertEquals("LOCALTENANT", response1.info.name);
    }
    @Test
    public void updateNotExistingTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        tenantInfo1.name = "TENANT-TEST-UPDATED";
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
                .put("/v1/tenants/{id}", currentMockId)
                .then()
                .statusCode(404)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("There is no Tenant with the following id:  " + currentMockId, response1.message);
    }

    @Test
    public void updateTenantForbiddenUser() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = createTenant("LOCALTENANT");

        //var webApi = new ArgoWebApiRequest();

        var request1 = new TenantRequestDto();
        var tenantInfo1 = new TenantInfoDto();
        var tenantContact1 = new ContactDto();

        request.info.name = "LOCALTENANT";
        tenantInfo1.email = "test2-updated@gmail.com";
        tenantInfo1.description = "this is test2 updated tenant description";
        tenantInfo1.image = "https://example/image.png";
        tenantInfo1.website = "https://test2.updated.tenant.org";


        tenantContact1.name = "Test User";
        tenantContact1.email = "test@gmail.com";
        tenantContact1.type = "ADMIN";

        request1.info = tenantInfo1;
        request1.contacts = Collections.singletonList(tenantContact1);

        var response1 = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request1)
                .contentType(ContentType.JSON)
                .when()
                .put("/v1/tenants/{id}", request.id)
                .then()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Access denied.", response1.message);
    }

    @Test
    public void viewTenants() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var tenant = createTenant("LOCALTENANT");

        var list = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .get("/v1/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertEquals(1, list.getContent().size());
    }

    @Test
    public void testGetProjectsByTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var request = createTenant("LOCALTENANT");

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
        assignReq.tenantId = request.id;
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
                .get("/v1/tenants/" + request.id + "/projects")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
    }

    @Test
    public void fetchReports() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here

        var tenant = createTenant("LOCALTENANT");

        var reports  = given()
                .auth()
                .oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}/reports", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PartialReportResponseDto[].class);

        assertNotNull(reports);
        assertTrue(reports.length > 0);
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
    public void checkSlugNotExists() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        var tenant = createTenant("LOCALTENANT");

        var existResponse = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}/pages/check-slug/{slug}", tenant.id, "check-this-slug")
                .then()
                .statusCode(200)
                .extract()
                .as(ExistResponseDto.class);

        assertFalse(existResponse.exist);
    }

    @Test
    public void createStatusPage() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        var tenant = createTenant("LOCALTENANT");

        var request = buildValidStatusPageRequest("test-page-" + UUID.randomUUID());

        var created = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        assertNotNull(created.id);
        assertEquals(request.slug, created.slug);
        assertEquals(request.name, created.name);
        assertEquals(request.config.title, created.config.title);
    }

    @Test
    public void getStatusPage() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        var tenant = createTenant("LOCALTENANT");

        var request = buildValidStatusPageRequest("get-page-" + UUID.randomUUID());

        var created = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var fetched = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}/pages/{pageId}", tenant.id, created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(StatusPageResponseDto.class);

        assertEquals(created.id, fetched.id);
        assertEquals(created.slug, fetched.slug);
    }

    @Test
    public void getStatusPageForUserAndRole() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        var tenant = createTenant("LOCALTENANT");

        var viewerPage = buildValidStatusPageRequest("viewer-page" + UUID.randomUUID());

        given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(viewerPage)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var adminPage = buildValidStatusPageRequest("admin-page" + UUID.randomUUID());

        given()
                .auth().oauth2(tenantAdmin)
                .contentType(ContentType.JSON)
                .body(adminPage)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var adminPage1 = buildValidStatusPageRequest("admin-page1" + UUID.randomUUID());

        given()
                .auth().oauth2(tenantAdmin)
                .contentType(ContentType.JSON)
                .body(adminPage1)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var viewerResp = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/users/pages?page=1&size=10")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertNotNull(viewerResp.getContent());
        assertEquals(1, viewerResp.getContent().size());

        var adminResp = given()
                .auth().oauth2(tenantAdmin)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/users/pages?page=1&size=10")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertNotNull(adminResp.getContent());
        assertEquals(3, adminResp.getContent().size());

        var superAdminResp = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/users/pages?page=1&size=10")
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertNotNull(superAdminResp.getContent());
        assertEquals(3, superAdminResp.getContent().size());
    }

    @Test
    public void updateStatusPage() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        var tenant = createTenant("LOCALTENANT");

        var request = buildValidStatusPageRequest("update-page-" + UUID.randomUUID());

        var created = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var update = buildValidStatusPageUpdateRequest(created.slug);
        update.config.title = "Updated Config Title";
        update.config.description = "Updated description text";
        update.config.theming.color = "#00ff00";

        var updated = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/v1/tenants/{id}/pages/{pageId}", tenant.id, created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(StatusPageResponseDto.class);

        assertEquals("Updated Page", updated.name);
        assertEquals("Updated Config Title", updated.config.title);
        assertEquals("Updated description text", updated.config.description);
        assertEquals("#00ff00", updated.config.theming.color);
    }

    @Test
    public void listStatusPages() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        var tenant = createTenant("LOCALTENANT");

        var request = buildValidStatusPageRequest("list-page-" + UUID.randomUUID());

        given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .statusCode(201);

        var list = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}/pages?page=1&size=10", tenant.id)
                .then()
                .statusCode(200)
                .extract()
                .as(PageResource.class);

        assertNotNull(list.getContent());
        assertFalse(list.getContent().isEmpty());
    }

    @Test
    public void deleteStatusPage() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        var tenant = createTenant("LOCALTENANT");

        var request = buildValidStatusPageRequest("delete-page-" + UUID.randomUUID());

        var created = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/tenants/{id}/pages", tenant.id)
                .then()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var resp = given()
                .auth().oauth2(tenantViewer)
                .when()
                .delete("/v1/tenants/{id}/pages/{pageId}", tenant.id, created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, resp.code);
        assertTrue(resp.message.toLowerCase().contains("deleted"));
    }

    private TenantResponseDto createTenant(String tenantName) {
        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        var tenantContact = new ContactDto();
        tenantInfo.name = tenantName;
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";

        tenantContact.email = "test@gmail.com";
        tenantContact.name = "Test user";
        tenantContact.type = "ADMIN";

        request.info = tenantInfo;
        request.contacts = Collections.singletonList(tenantContact);

        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);
    }


    private ProjectRequestDto buildCreateRequest() {

        var dto = new ProjectRequestDto();
        dto.name = "Test Project" + UUID.randomUUID();
        dto.description = "Project Description";
        dto.startDate = Timestamp.from(Instant.now());
        dto.endDate = Timestamp.from(Instant.now());
        dto.sustainabilityEndDate = Timestamp.from(Instant.now());
        dto.dataRetentionPolicy = "Retention policy text";

        return dto;
    }

    private StatusPageRequestDto buildValidStatusPageRequest(String slug) {
        var req = new StatusPageRequestDto();
        req.name = "Test Page";
        req.slug = slug;
        req.reportId = "8aa28cec-2940-4fcf-ad95-57fbdaf5bbad";

        // groups must match mocks/status-groups.json names
        var atland = new StatusGroupResponseDto();
        atland.name = "ATLAND";
        atland.status = "CRITICAL";

        var arnes = new StatusGroupResponseDto();
        arnes.name = "ARNES";
        arnes.status = "OK";

        var group1 = new StatusPageGroupDto();
        group1.name = "group-1";
        group1.alias = "Group A";
        group1.list = List.of(atland, arnes);

        var theming = new StatusPageThemingDto();
        theming.logo = "";
        theming.color = "#ffffff";
        theming.status = new StatusPageThemingStatusDto();
        theming.status.icon = "led";
        theming.status.text = "badge";
        theming.columns = "one";

        var config = new StatusPageConfigDto();
        config.title = "Test Title";
        config.description = "Test description";
        config.groups = List.of(group1);
        config.theming = theming;

        req.config = config;
        return req;
    }

    private StatusPageUpdateRequestDto buildValidStatusPageUpdateRequest(String slug) {
        var req = new StatusPageUpdateRequestDto();
        req.name = "Updated Page";
        req.slug = slug;
        req.reportId = "8aa28cec-2940-4fcf-ad95-57fbdaf5bbad";

        // same structure as create
        var atland = new StatusGroupResponseDto();
        atland.name = "ATLAND";
        atland.status = "CRITICAL";

        var group1 = new StatusPageGroupDto();
        group1.name = "group-1";
        group1.alias = "Group A";
        group1.list = List.of(atland);

        var theming = new StatusPageThemingDto();
        theming.logo = "";
        theming.color = "#00ff00";
        theming.status = new StatusPageThemingStatusDto();
        theming.status.icon = "led";
        theming.status.text = "badge";
        theming.columns = "one";

        var config = new StatusPageConfigDto();
        config.title = "Updated Title";
        config.description = "Updated description";
        config.groups = List.of(group1);
        config.theming = theming;

        req.config = config;
        return req;
    }





//    @Test
//    public void fetchReportById() {
//        var request = createTenant("MOCK-TENANT");
//
//        var response = given()
//                .auth()
//                .oauth2(tenantViewer)
//                .contentType(ContentType.JSON)
//                .when()
//                .get("/{id}/reports/{report-id}", request.id, "any-id")
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(FullReportResponseDto.class);
////
////        assertEquals("MOCK-TENANT", response.getTenant());
//
//    }


    private WebApiReportResponse loadMockReport() throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mocks/mock-report.json")) {
            return new ObjectMapper().readValue(is, WebApiReportResponse.class);
        }
    }

    private WebApiReportResponse loadMockReports() throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mocks/reports.json")) {
            return new ObjectMapper().readValue(is, WebApiReportResponse.class);
        }
    }

    private ArgoStatusGroupsResponse loadMockStatusGroups() throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mocks/status-groups.json")) {
            return new ObjectMapper().readValue(is, ArgoStatusGroupsResponse.class);
        }
    }


}