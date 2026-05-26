package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.GroupUserResponse;
import org.grnet.endpoint.scanner.runtime.repositories.RoleEndpointRepository;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.status.api.endpoints.AdminEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.dtos.ams.PublishResponse;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.project.ProjectUpdateDto;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.status.EventStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusFullResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.enums.EventStatus;
import org.grnet.status.enums.TenantJobEvent;
import org.grnet.status.services.AuthGroupSetupService;
import org.grnet.status.services.GroupManagementService;
import org.grnet.status.services.clients.AmsClient;
import org.grnet.status.services.clients.AmsClientFactory;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@TestHTTPEndpoint(AdminEndpoint.class)
public class AdminEndpointTest extends KeycloakTest {

    @InjectMock
    GroupManagementService groupManagementService;

    @InjectMock
    AuthGroupSetupService authGroupSetupService;
    @InjectMock
    AmsClientFactory amsClientFactory;
    @InjectMock
    @RestClient
    ArgoWebApiClient argoWebApiClient;

    private String currentMockId;

    @Inject
    TestEntitlementProvider entitlementProvider;

    @Inject
    RoleEndpointRepository roleEndpointRepository;

    // -------------------------------------------------------------------------
    // SETUP ROLE REPOSITORY
    // -------------------------------------------------------------------------
    @BeforeEach
    void setupRepo() {
        org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository testRepo = new org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository();
        QuarkusMock.installMockForType(testRepo, RoleEndpointRepository.class);
        this.roleEndpointRepository = testRepo;
    }

    // -------------------------------------------------------------------------
    // RESET STATE
    // -------------------------------------------------------------------------
    @BeforeEach
    void reset() {
        entitlementProvider.reset();
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).reset();
    }

    private void mockSuperAdmin() {
        entitlementProvider.setSuperAdmin(true);
        entitlementProvider.setEntitlements(List.of());
    }

    private void mockTenantAdmin() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(
                entitlement(currentMockId, "tenant_admin")
        ));
    }

    private void mockTenantViewer() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(
                entitlement(currentMockId, "tenant_viewer")
        ));
    }

    private Entitlement entitlement(String tenantId, String role) {
        String raw =
                "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:"
                        + role
                        + ":TENANT:"
                        + tenantId
                        + ":role=member";

        return new Entitlement(
                "status-pages",
                List.of("status-pages", role, "TENANT", tenantId),
                role,
                raw
        );
    }

    @BeforeEach
    public void mockGroupAsyncService() {
        doNothing().when(authGroupSetupService)
                .createGroup(anyString(), anyString(), anyList(), anyMap());

        doNothing().when(authGroupSetupService)
                .deleteGroup(anyString());
    }

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
    }

    @BeforeEach
    void mockAmsClient() {
        var mockClient = mock(AmsClient.class);

        when(amsClientFactory.buildClient(anyString()))
                .thenReturn(mockClient);

        var resp = new PublishResponse();
        resp.setMessageIds(List.of("mock-message"));

        when(mockClient.publish(anyString(), anyString(), anyString(), any(PublishRequest.class)))
                .thenReturn(resp);
    }

    @BeforeEach
    public void cleanUp() {
        tenantService.deleteAll();
    }


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
        info.setName("TENANT-TEST");
        info.setEmail("test@gmail.com");
        info.setDescription("this is test tenant description");
        info.setImage("https://example/image.png");
        info.setWebsite("https://test.tenant.org");
        info.setUpdated("2025-01-02 00:00:00");
        data.setInfo(info);
        tenantWebApiResponse.getData().add(data);
        data.setInfo(info);
        var status = new Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }

    @Test
    public void createTenant() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        assertEquals(request.info.name, request.info.name);
        assertEquals(currentMockId, request.id);  // check that the id matches what you set
    }


    @Test
    public void deleteTenant() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        var response1 = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", request.id)
                .then()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(200, response1.code);
    }


    @Test
    public void deleteTenantForbidden() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

        //var webApi = new ArgoWebApiRequest();
        mockTenantViewer();
        var error = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .when()
                .delete("/tenants/{id}", request.id)
                .then()
                .statusCode(403)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(403, error.code);
        assertEquals("Access denied.", error.message);

    }

    @Test
    public void deleteTenantNotExisting() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";  // dynamically set here
        mockSuperAdmin();
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
    public void testAssignMultipleProjects() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
        mockSuperAdmin();
        var request = createTenant("LOCALTENANT");

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
        req.tenantId = request.id;
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
    public void testAssignInvalidTenant() {
        var req = new TenantProjectRequestDto();
        req.tenantId = "tenant-not-exist";
        req.projectIds = List.of("proj-not-exist");
        mockSuperAdmin();
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
    public void updateTenantStatus() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        var tenantContact = new ContactDto();
        tenantInfo.name = "TENANT-TEST";
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";
        tenantContact.email = "test@gmail.com";
        tenantContact.name = "Test user";
        tenantContact.type = "ADMIN";

        request.info = tenantInfo;
        request.contacts = Collections.singletonList(tenantContact);
        mockSuperAdmin();

        var created = given()
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

        var statusReq = new TenantStatusDto();
        statusReq.jobs = new ArrayList<>();

        var job = new EventStatusDto();
        job.name = TenantJobEvent.CREATE_DOMAIN_NAMES.name();
        job.status = EventStatus.COMPLETED.name(); // "completed"
        job.message = "Creating domain names";
        job.start = Instant.parse("2025-10-22T12:44:48.107Z");
        job.end = Instant.parse("2025-10-22T12:44:48.107Z");
        statusReq.jobs.add(job);

        var updated = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(statusReq)
                .when()
                .patch("/tenants/{id}/manual/status", created.id)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantStatusFullResponse.class);

        assertEquals(8, updated.status.jobs.size());
        assertEquals(job.name, updated.status.jobs.get(2).name);
        assertEquals(job.status, updated.status.jobs.get(2).status);
    }

    @Test
    public void testCreateProject() {
        var req = buildCreateRequest();
        mockSuperAdmin();
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
        mockSuperAdmin();
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
        mockSuperAdmin();
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
        update.description = "Project description UPDATE";
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

        assertEquals("Retention policy text", updated.dataRetentionPolicy);
    }

    @Test
    public void testDeleteProject() {
        var req = buildCreateRequest();
        mockSuperAdmin();
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

    @Test
    public void superAdminFetchAllMembers() {

        mockSuperAdmin();

        var fakeUsers = List.of(
                createFakeUser("1", "user1"),
                createFakeUser("2", "user2"),
                createFakeUser("3", "user3"),
                createFakeUser("4", "user4"),
                createFakeUser("5", "user5")
        );

        var fakePageableResponse = createFakePageableResponse(fakeUsers);

        when(groupManagementService.getAllMembers(
                eq("members"),
                any(),
                eq(0),
                eq(5),
                any()
        )).thenReturn(fakePageableResponse);

        var response = given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/members?page=1&size=5")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(AdminEndpoint.PageableGroupUserResponse.class);

        assertNotNull(response.getContent());
        assertEquals(5, response.getContent().size());
    }

    private AdminEndpoint.PageableGroupUserResponse createFakePageableResponse(List<GroupUserResponse> users) {
        var response = new AdminEndpoint.PageableGroupUserResponse();
        response.setContent(users);
        response.setSizeOfPage(5);
        response.setNumberOfPage(1);
        response.setTotalElements(5L);
        response.setTotalPages(1);
        response.setLinks(List.of());
        return response;
    }

    private GroupUserResponse createFakeUser(String id, String username) {
        var user = new GroupUserResponse();
        user.id = id;
        user.username = username;
        user.firstName = "First_" + username;
        user.lastName = "Last_" + username;
        user.email = username + "@test.com";
        user.uid = "uid_" + id;
        user.tenants = List.of(); // ή fake tenants αν χρειαστεί
        return user;
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
                .post("/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);
    }

    private ProjectRequestDto buildCreateRequest() {

        var dto = new ProjectRequestDto();
        dto.name = "Test Project" + UUID.randomUUID();
        dto.description = "Project description";
        dto.startDate = Timestamp.from(Instant.now());
        dto.endDate = Timestamp.from(Instant.now());
        dto.sustainabilityEndDate = Timestamp.from(Instant.now());
        dto.dataRetentionPolicy = "Retention policy text";

        return dto;
    }

}