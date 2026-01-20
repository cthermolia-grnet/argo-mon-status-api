package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationActionResponse; // (yes, name is weird, but this is your endpoint)
import org.grnet.status.dtos.tenant.invitations.TenantInvitationRequest;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiCreateResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.enums.InvitationAction;
import org.grnet.status.enums.InvitationStatus;
import org.grnet.status.services.MailerService;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class TenantInvitationEndpointTest extends KeycloakTest {

    @InjectMock
    ArgoWebApiClientFactory argoWebApiClientFactory;

    @InjectMock
    MailerService mailerService;

    private String currentMockId;

    @BeforeEach
    public void mockArgoClient() throws Exception {
        var mockClient = org.mockito.Mockito.mock(ArgoWebApiClient.class);

        when(mockClient.createTenant(any(), any())).thenAnswer(invocation -> loadMockTenantResponse(currentMockId));
        when(mockClient.getTenant(any(), any())).thenAnswer(invocation -> loadMockTenantGetResponse(currentMockId));

        when(argoWebApiClientFactory.buildClient(anyString())).thenReturn(mockClient);
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

    @Test
    public void createInvitationMailNotifications() {

        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");
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

        var tenant = createTenant("LOCALTENANT");
        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        var fetched = getInvitationByIdAsInvitedUser(created.id);

        assertEquals("PENDING", String.valueOf(fetched.status));
        assertEquals("local-viewer@test.dev", fetched.email);
        assertEquals("viewer", fetched.role);
        assertEquals(tenant.id, fetched.tenantId);
    }

    @Test
    public void getAllInvitationsUser() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");

        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        var page = getInvitationsPagedAsInvitedUser(1, 10);

        assertNotNull(page);
        assertNotNull(page.getContent());

        assertEquals(1, page.getContent().size());
        assertTrue(page.getContent().stream().allMatch(i -> "local-viewer@test.dev".equals(i.email)));
    }

    @Test
    public void getAllInvitationsTenantAdmin() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");

        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
        createInvitation(tenant.id, "local-test@test.dev", "viewer");
        createInvitation(tenant.id, "local-local@test.dev", "viewer");

        var page = getInvitationsPagedAsTenantAdmin(tenant.id,1, 10);

        assertNotNull(page);
        assertNotNull(page.getContent());

        assertEquals(3, page.getContent().size());
    }

    @Test
    public void getAllInvitationsSuperAdmin() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");

        createInvitation(tenant.id, "local-viewer@test.dev", "viewer");
        createInvitation(tenant.id, "local-admin@test.dev", "admin");


        var page = getAllInvitationsAsSuperAdmin(1, 10);

        assertNotNull(page);
        assertNotNull(page.getContent());

        assertEquals(2, page.getContent().size());
    }


    @Test
    public void acceptInvitation() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");
        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        var response = respondToInvitationAsInvitedUser(created.id, "ACCEPT");

        assertEquals("ACCEPTED", String.valueOf(response.status));
    }

    @Test
    public void acceptInvitationMailNotificationUser() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");

        var createdAdmin = createInvitation(tenant.id, "local-admin@test.dev", "admin");
        var createdViewer = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        var response = respondToInvitationAsInvitedUser(createdViewer.id, "ACCEPT");


        verify(mailerService, times(1)).sendInvitationAcceptedToInvitee(
                argThat(list -> list != null && list.contains("local-viewer@test.dev")),
                eq("LOCALTENANT"),
                eq("viewer"),
                anyString()
        );

        // admins do NOT get email (because AGM returned none)
        verify(mailerService, never()).sendInvitationResponseToAdmins(
                anyList(), anyString(), anyString(), anyString(), any(), anyString()
        );

        assertEquals("ACCEPTED", String.valueOf(response.status));
    }

    @Test
    public void rejectInvitation() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");
        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        var response = respondToInvitationAsInvitedUser(created.id, "REJECT");

        assertEquals("REJECTED", String.valueOf(response.status));
    }

    @Test
    public void alreadyRespondedInvitation() {
        currentMockId = "e1ab046c-8544-47e6-bd8f-e8aa8b83acb3";

        var tenant = createTenant("LOCALTENANT");
        var created = createInvitation(tenant.id, "local-viewer@test.dev", "viewer");

        respondToInvitationAsInvitedUser(created.id, "ACCEPT");

        var req = buildActionRequest("REJECT");

        given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .patch("/v1/users/invitations/{id}", created.id)
                .then()
                .statusCode(409);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Helpers (keep tests small + consistent)
    // -----------------------------------------------------------------------------------------------------------------

    private TenantResponseDto createTenant(String tenantName) {
        var request = new TenantRequestDto();
        var tenantInfo = new TenantInfoDto();
        tenantInfo.name = tenantName;
        tenantInfo.email = "test@gmail.com";
        tenantInfo.description = "this is test tenant description";
        tenantInfo.image = "https://example/image.png";
        tenantInfo.website = "https://test.tenant.org";
        request.info = tenantInfo;

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

    private PageableInvitations getInvitationsPagedAsTenantAdmin(String invitationId, int page, int size) {
        return given()
                .auth()
                .oauth2(tenantAdmin)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/tenants/{id}/invitations?page={page}&size={size}", invitationId, page, size)
                .then()
                .statusCode(200)
                .extract()
                .as(PageableInvitations.class);
    }

    private PageableInvitations getInvitationsPagedAsInvitedUser(int page, int size) {
        return given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/users/invitations?page={page}&size={size}", page, size)
                .then()
                .statusCode(200)
                .extract()
                .as(PageableInvitations.class);
    }

    private PageableInvitations getAllInvitationsAsSuperAdmin(int page, int size) {
        return given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/v1/admin/invitations?page={page}&size={size}", page, size)
                .then()
                .statusCode(200)
                .extract()
                .as(PageableInvitations.class);
    }

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

    private TenantInvitationActionResponse buildActionRequest(String action) {
        var req = new TenantInvitationActionResponse();
        req.action = InvitationAction.valueOf(action);

        return req;
    }

    public static class PageableInvitations extends PageResource<TenantInvitationResponse> {
        private List<TenantInvitationResponse> content;

        @Override
        public List<TenantInvitationResponse> getContent() {
            return content;
        }

        @Override
        public void setContent(List<TenantInvitationResponse> content) {
            this.content = content;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Mock payloads (unchanged)
    // -----------------------------------------------------------------------------------------------------------------

    private TenantWebApiCreateResponse loadMockTenantResponse(String id) {
        var tenantWebApiResponse = new TenantWebApiCreateResponse();
        var data = new TenantWebApiCreateResponse.Data();
        var link = new TenantWebApiCreateResponse.Links();
        var status = new TenantWebApiCreateResponse.Status();
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
        info.setName("TENANT TEST");
        info.setEmail("test@gmail.com");
        info.setDescription("this is test tenant description");
        info.setImage("https://example/image.png");
        info.setWebsite("https://test.tenant.org");
        info.setUpdated("2025-01-02 00:00:00");
        data.setInfo(info);
        tenantWebApiResponse.getData().add(data);
        var status = new TenantWebApiGetResponse.Status();
        status.setCode("200");
        status.setMessage("Τenant was succesfully created");
        tenantWebApiResponse.setStatus(status);
        return tenantWebApiResponse;
    }
}
