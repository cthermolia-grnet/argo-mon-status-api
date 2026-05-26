package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.repositories.RoleEndpointRepository;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.status.api.endpoints.TenantInvitationEndpoint;
import org.grnet.status.dtos.Status;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationActionResponse;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationRequest;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.enums.InvitationAction;
import org.grnet.status.services.MailerService;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class TenantInvitationEndpointTest extends KeycloakTest {

    @InjectMock
    @RestClient
    ArgoWebApiClient argoWebApiClient;
    @InjectMock
    MailerService mailerService;

    private String currentMockId;

    @Inject
    TestEntitlementProvider entitlementProvider;

    @Inject
    RoleEndpointRepository roleEndpointRepository;

    @BeforeEach
    void setupRepo() {
        org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository testRepo = new org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository();

        QuarkusMock.installMockForType(testRepo, RoleEndpointRepository.class);

        this.roleEndpointRepository = testRepo;
    }
    @BeforeEach
    public void mockArgoClient() throws Exception {
        when(argoWebApiClient.createTenant(any(), any())).thenAnswer(invocation -> loadMockTenantResponse(currentMockId));
        when(argoWebApiClient.getTenant(any(), any())).thenAnswer(invocation -> loadMockTenantGetResponse(currentMockId));
    }

    @BeforeEach
    void setupMailerMock() {
        doNothing().when(mailerService).sendTenantInvitationEmail(anyList(), anyString(), anyString(), anyString());
        doNothing().when(mailerService).sendInvitationAcceptedToInvitee(anyList(), anyString(), anyString(), anyString());
        doNothing().when(mailerService).sendInvitationResponseToAdmins(anyList(), anyString(), anyString(), anyString(), any(), anyString());
    }

    @BeforeEach
    public void cleanUp() {
        tenantService.deleteAll();
        tenantInvitationService.deleteAll();
    }
    @BeforeEach
    void reset() {
        entitlementProvider.reset();
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).reset();    }
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


    @Test
    public void createInvitationMailNotifications() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        mockTenantAdmin();
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/invitation",
                        LocalDateTime.now(),
                        null
                )
        ));

        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        verify(mailerService, times(1)).sendTenantInvitationEmail(
                eq(List.of("local-viewer@test.dev")),
                eq("LOCALTENANT"),
                eq("viewer"),
                anyString()
        );
    }

    @Test
    public void getInvitation() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        mockTenantAdmin();

        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/invitation",
                        LocalDateTime.now(),
                        null
                )
        ));

        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        // IMPORTANT: switch identity for GET (viewer side)
        mockTenantViewer();


        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_viewer",
                        "tenant_viewer",
                        "GET/v1/users/{id}/invitation",
                        LocalDateTime.now(),
                        null
                )
        ));
        var fetched = getInvitationByIdAsInvitedUser(created.id);

        assertEquals("PENDING", String.valueOf(fetched.status));
        assertEquals("local-viewer@test.dev", fetched.email);
        assertEquals("viewer", fetched.role);
        assertEquals(tenant.id, fetched.tenantId);
    }

    @Test
    public void getAllInvitationsUser() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        mockTenantAdmin();
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/invitation",
                        LocalDateTime.now(),
                        null
                )
        ));

        // create data
        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        // IMPORTANT: mock interceptor role endpoint lookup
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository)
                .set(List.of(
                        new RoleEndpoint(
                                1L,
                                "tenant_viewer",
                                "tenant_viewer",
                                "GET_/v1/users/invitations",
                                LocalDateTime.now(),
                                null
                        )
                ));

        // call API
        var page = getInvitationsPagedAsInvitedUser(1, 10);

        // assertions
        assertNotNull(page);
        assertNotNull(page.getContent());

        assertEquals(1, page.getContent().size());
        assertTrue(page.getContent()
                .stream()
                .allMatch(i -> "local-viewer@test.dev".equals(i.email)));
    }
    //
    @Test
    public void getAllInvitationsTenantAdmin() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin();

        var tenant = createTenant("LOCALTENANT");

        mockTenantAdmin();
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/invitation",
                        LocalDateTime.now(),
                        null
                )
        ));

        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
        createInvitation(tenant.id, "local-test@test.dev", "viewer");
        createInvitation(tenant.id, "local-local@test.dev", "viewer");

        // IMPORTANT: mock interceptor authorization
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository)
                .set(List.of(
                        new RoleEndpoint(
                                1L,
                                "tenant_admin",
                                "tenant_admin",
                                "GET_/v1/tenants/{id}/invitations",
                                LocalDateTime.now(),
                                null
                        )
                ));

        var page = getInvitationsPagedAsTenantAdmin(tenant.id, 1, 10);

        assertNotNull(page);
        assertNotNull(page.getContent());

        assertEquals(3, page.getContent().size());
    }

    //
//    @Test
//    public void getAllInvitationsSuperAdmin() {
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
//
//        var tenant = createTenant("LOCALTENANT");
//
//        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
//        createInvitation(tenant.id, "local-admin@test.dev", "admin");
//
//
//        var page = getAllInvitationsAsSuperAdmin(1, 10);
//
//        assertNotNull(page);
//        assertNotNull(page.getContent());
//
//        assertEquals(2, page.getContent().size());
//    }
//
//
//    @Test
//    public void acceptInvitation() {
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
//
//        var tenant = createTenant("LOCALTENANT");
//        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
//
//        var response = respondToInvitationAsInvitedUser(created.id, "ACCEPT");
//
//        assertEquals("ACCEPTED", String.valueOf(response.status));
//    }
//
//    @Test
//    public void acceptInvitationMailNotificationUser() {
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
//
//        var tenant = createTenant("LOCALTENANT");
//
//        var createdAdmin = createInvitation(tenant.id, "local-admin@test.dev", "admin");
//        var createdViewer = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
//
//        var response = respondToInvitationAsInvitedUser(createdViewer.id, "ACCEPT");
//
//
//        verify(mailerService, times(1)).sendInvitationAcceptedToInvitee(
//                argThat(list -> list != null && list.contains("local-viewer@test.dev")),
//                eq("LOCALTENANT"),
//                eq("viewer"),
//                anyString()
//        );
//
//        // admins do NOT get email (because AGM returned none)
//        verify(mailerService, never()).sendInvitationResponseToAdmins(
//                anyList(), anyString(), anyString(), anyString(), any(), anyString()
//        );
//
//        assertEquals("ACCEPTED", String.valueOf(response.status));
//    }
//
//    @Test
//    public void rejectInvitation() {
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
//
//        var tenant = createTenant("LOCALTENANT");
//        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
//
//        var response = respondToInvitationAsInvitedUser(created.id, "REJECT");
//
//        assertEquals("REJECTED", String.valueOf(response.status));
//    }
//
//    @Test
//    public void revokeInvitation() {
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
//
//        var tenant = createTenant("LOCALTENANT");
//        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
//
//        var revoke = revokeInvitation(currentMockId, created.id);
//
//        assertEquals("REVOKED", String.valueOf(revoke.status));
//    }
//
//    // -----------------------------------------------------------------------------------------------------------------
//    // Helpers (keep tests small + consistent)
//    // -----------------------------------------------------------------------------------------------------------------
//
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
                .post("v1/admin/tenants")
                .then()
                .statusCode(200)
                .extract()
                .as(TenantResponseDto.class);
    }

    private TenantInvitationResponse createInvitation(String tenantId, String email, String role) {
        var invitation = new TenantInvitationRequest();
        invitation.email = email;
        invitation.role = role;

        return given()
                .auth().oauth2(tenantAdmin)
                .contentType(ContentType.JSON)
                .body(invitation)
                .when()
                .post("v1/tenants/{id}/invitation", tenantId)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantInvitationResponse.class);

    }
    //
    private TenantInvitationResponse getInvitationByIdAsInvitedUser(String invitationId) {
        return given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/users/invitations/{id}", invitationId)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantInvitationResponse.class);
    }
    //
    private TenantInvitationEndpoint.PageableInvitations getInvitationsPagedAsTenantAdmin(String invitationId, int page, int size) {
        return given()
                .auth()
                .oauth2(tenantAdmin)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}/invitations?page={page}&size={size}", invitationId, page, size)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantInvitationEndpoint.PageableInvitations.class);
    }
    //
    private TenantInvitationEndpoint.PageableInvitations getInvitationsPagedAsInvitedUser(int page, int size) {
        return given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/users/invitations?page={page}&size={size}", page, size)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantInvitationEndpoint.PageableInvitations.class);
    }

    @Test
    public void getAllInvitationsAsSuperAdmin() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        mockSuperAdmin(); // IMPORTANT

        var tenant = createTenant("LOCALTENANT");

        mockTenantAdmin();
        ((org.grnet.endpoint.scanner.runtime.repositories.TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
                new RoleEndpoint(
                        1L,
                        "tenant_admin",
                        "tenant_admin",
                        "POST_/v1/tenants/{id}/invitation",
                        LocalDateTime.now(),
                        null
                )
        ));

        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
        createInvitation(tenant.id, "local-admin@test.dev", "admin");

        var page = getAllInvitationsAsSuperAdmin(1, 10);

        assertNotNull(page);
        assertNotNull(page.getContent());

        assertEquals(2, page.getContent().size());
    }

    //
    private TenantInvitationEndpoint.PageableInvitations getAllInvitationsAsSuperAdmin(int page, int size) {
        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/admin/invitations?page={page}&size={size}", page, size)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantInvitationEndpoint.PageableInvitations.class);
    }
//    @Test
//    public void acceptInvitation() {
//
//        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";
//
//        mockSuperAdmin();
//
//        var tenant = createTenant("LOCALTENANT");
//
//        mockTenantAdmin();
//        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of(
//                new RoleEndpoint(
//                        1L,
//                        "tenant_admin",
//                        "tenant_admin",
//                        "POST_/v1/tenants/{id}/invitation",
//                        LocalDateTime.now()
//                )
//        ));
//
//        var created = createInvitation(
//                tenant.id,
//                "local-viewer@test.dev",
//                "viewer"
//        );
//
//        mockTenantViewer();
//
//        ((TestRoleEndpointRepository) roleEndpointRepository)
//                .set(List.of(
//                        new RoleEndpoint(
//                                1L,
//                                "tenant_viewer",
//                                "tenant_viewer",
//                                "PATCH_/v1/users/invitations/{id}",
//                                LocalDateTime.now()
//                        )
//                ));
//
//        var response = respondToInvitationAsInvitedUser(
//                created.id,
//                "ACCEPT"
//        );
//
//        assertEquals("ACCEPTED", String.valueOf(response.status));
//    }

    //
    private TenantInvitationResponse respondToInvitationAsInvitedUser(String invitationId, String action) {
        var req = buildActionRequest(action);

        return given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .patch("/v1/users/invitations/{id}", invitationId)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantInvitationResponse.class);
    }
    //
    private TenantInvitationResponse revokeInvitation(String tenantId, String invitationId) {

        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .patch("/v1/tenants/{id}/invitations/{invitation_id}", tenantId, invitationId)
                .then()
                .statusCode(200)
                .extract()
                .as(TenantInvitationResponse.class);
    }
    //
    private TenantInvitationActionResponse buildActionRequest(String action) {
        var req = new TenantInvitationActionResponse();
        req.action = InvitationAction.valueOf(action);

        return req;
    }
    ////
//    public static class PageableInvitations extends PageResource<TenantInvitationResponse> {
//        private List<TenantInvitationResponse> content;
//
//        @Override
//        public List<TenantInvitationResponse> getContent() {
//            return content;
//        }
//
//        @Override
//        public void setContent(List<TenantInvitationResponse> content) {
//            this.content = content;
//        }
//    }
//
//    // -----------------------------------------------------------------------------------------------------------------
//    // Mock payloads (unchanged)
//    // -----------------------------------------------------------------------------------------------------------------

    private TenantWebApiCreateResponse loadMockTenantResponse(String id) {
        var tenantWebApiResponse = new TenantWebApiCreateResponse();
        var data = new TenantWebApiCreateResponse.Data();
        var link = new TenantWebApiCreateResponse.Links();
        var status = new Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        link.setSelf("https://https://test.api.grnet.gr/api/v2/admin/tenants/" + id);
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
        info.setName("TENANT_TEST");
        info.setEmail("test@gmail.com");
        info.setDescription("this is test tenant description");
        info.setImage("https://example/image.png");
        info.setWebsite("https://test.tenant.org");
        info.setUpdated("2025-01-02 00:00:00");
        data.setInfo(info);
        tenantWebApiResponse.getData().add(data);
        var status = new Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }
}
