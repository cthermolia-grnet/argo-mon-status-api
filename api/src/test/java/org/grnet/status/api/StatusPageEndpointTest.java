package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.api.endpoints.StatusPageEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.argo.ArgoReportsResponse;
import org.grnet.status.dtos.argo.ArgoStatusGroupsResponse;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.grnet.status.dtos.status.StatusGroupResponseDto;
import org.grnet.status.dtos.statuspage.*;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.utils.EncryptUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(StatusPageEndpoint.class)
public class StatusPageEndpointTest extends KeycloakTest {

    @ConfigProperty(name = "web.api.url")
    String webapi;
    @ConfigProperty(name = "web.api.access.token")
    String webApiToken;

    @InjectMock
    @RestClient
    ArgoWebApiClient argoWebApiClient;
    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadLogoDir;

    @Inject
    EncryptUtil encryptUtil;

    @BeforeEach
    public void mockArgoClient() throws Exception {
        when(argoWebApiClient.fetchReports(anyString())).thenReturn(loadMockReports());
        when(argoWebApiClient.fetchStatusGroups(any(), any())).thenReturn(loadMockStatusGroups());
    }


    private String encrypt() {
        return encryptUtil.encrypt(webApiToken);
    }

    @Test
    public void checkSlugNotExists() {
        var request = new StatusPageRequestDto();
        request.name = "Slug Check Page";
        request.slug = "check-this-slug";
        request.api = webapi;
        request.secret = encrypt();

        request.report = "Critical";

        var existResponse = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .get("/check-slug/{slug}", "check-this-slug")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(ExistResponseDto.class);

        assertEquals(existResponse.exist, false);
    }

    @Test
    public void createStatusPage() {

        var request = new StatusPageRequestDto();
        request.name = "Test Page AB";
        request.slug = "test-page-" + UUID.randomUUID();
        // request.api = "https://api.devel.mon.argo.grnet.gr";
        request.api = webapi;
        //  request.secret = "VaWi0ZBjGrxXPuB0o+KARpH63EKDaiwttfLE54POPtaw4QRxYktsabA+CT76sX0D";
        request.secret = encrypt();
        request.report = "Critical";

        createTestStatusPageDto(request);

        given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

    }

    @Test
    public void createStatusPageSlugExist() {

        var request = new StatusPageRequestDto();
        request.name = "Test Page AB";
        request.slug = "test-page-" + UUID.randomUUID();
        // request.api = "https://api.devel.mon.argo.grnet.gr";
        request.api = webapi;
        // request.secret = "VaWi0ZBjGrxXPuB0o+KARpH63EKDaiwttfLE54POPtaw4QRxYktsabA+CT76sX0D";

        request.secret = encrypt();
        request.report = "Critical";

        createTestStatusPageDto(request);

        given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(400)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("A page with slug '" + request.slug + "' already exists.", error.message);

    }

    @Test
    public void createStatusPageInvalidArgoConnection() {

        var request = new StatusPageRequestDto();
        request.name = "Bad ARGO Page";
        request.slug = "bad-argo-" + UUID.randomUUID();
        request.api = "https://api.fake.argo";
        request.secret = "wrong-secret";
        request.report = "Critical";

        // Ensure mock throws
//        when(argoWebApiClientFactory.buildClient(anyString()))
//                .thenThrow(new RuntimeException("connection error"));

        createTestStatusPageDto(request);

        var error = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(400)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Invalid ARGO API or Secret", error.message);
    }


    @Test
    public void createStatusPageInvalidGroupItemName() {

        var request = new StatusPageRequestDto();
        request.name = "Invalid Page";
        request.slug = "invalid-page-" + UUID.randomUUID();
        request.api = webapi;
        request.secret = encrypt();
        request.report = "Critical";

        System.out.println("SECRET IS : " + request.secret);

        // Create DTO but override a name to be invalid
        createTestStatusPageDto(request);
        request.config.groups.get(0).list.get(0).name = "INVALID-SITE";

        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(500)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Service 'INVALID-SITE' is not a valid ARGO item for report 'Critical'", error.message);
    }

    @Test
    public void createStatusPageInvalidGroupItemStatus() {

        var request = new StatusPageRequestDto();
        request.name = "Invalid Page";
        request.slug = "invalid-page-" + UUID.randomUUID();
        request.api = webapi;
        request.secret = encrypt();
        request.report = "Critical";

        // Create DTO but override a name to be invalid
        createTestStatusPageDto(request);
        request.config.groups.get(0).list.get(0).status = "INVALID-STATUS";

        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(500)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Invalid ARGO status 'INVALID-STATUS' for 'ATLAND'.", error.message);
    }


    @Test
    public void createStatusPageInvalidTheming() {

        var request = new StatusPageRequestDto();
        request.name = "Bad Color Page";
        request.slug = "bad-color-" + UUID.randomUUID();
        request.api = webapi;
        request.secret = encrypt();
        request.report = "Critical";

        createTestStatusPageDto(request);
        request.config.theming.color = "pink";

        var error = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(500)
                .extract()
                .as(InformativeResponse.class);

        assertEquals("Invalid color format, expected #RRGGBB", error.message);
    }
//    @Test
//    public void createStatusPageMissingRequiredFields() {
//        var request = new StatusPageRequestDto();
//        request.name = "";
//        request.slug = "";
//        request.api = "";
//        request.secret = "";
//        request.report = "";
//        request.config = null;
//
//        var response = given()
//                .auth().oauth2(aliceToken)
//                .contentType(ContentType.JSON)
//                .body(request)
//                .when()
//                .post()
//                .then()
//                .statusCode(400)
//                .extract()
//                .as(InformativeResponse.class);
//
//        assertEquals(400, response.code);
//        assert response.errors.contains("name cannot be blank");
//        assert response.errors.contains("slug cannot be blank");
//        assert response.errors.contains("api cannot be blank");
//        assert response.errors.contains("secret cannot be blank");
//        assert response.errors.contains("report cannot be blank");
//        assert response.errors.contains("config must not be null");
//    }
//
//    @Test
//    public void createStatusPageInvalidNestedConfig() {
//        // Minimal valid top-level
//        var request = new StatusPageRequestDto();
//        request.name = "Page with bad config";
//        request.slug = "invalid-config-page";
//        request.api = "https://api.devel.mon.argo.grnet.gr";
//        request.secret = "dummy";
//        request.report = "Critical";
//
//        // (empty title, no groups, missing theming)
//        var invalidConfig = new org.grnet.status.dtos.statuspage.StatusPageConfigDto();
//        invalidConfig.title = "";
//        invalidConfig.groups = java.util.Collections.emptyList();
//        invalidConfig.theming = null;
//        request.config = invalidConfig;
//
//        var response = given()
//                .auth().oauth2(aliceToken)
//                .contentType(ContentType.JSON)
//                .body(request)
//                .when()
//                .post()
//                .then()
//                .statusCode(400)
//                .extract()
//                .as(InformativeResponse.class);
//
//        assertEquals(400, response.code);
//        assert response.errors.contains("title cannot be blank");
//        assert response.errors.contains("groups cannot be empty");
//    }
//
//    @Test
//    public void createStatusPageInvalidNestedGroup() {
//        var request = new StatusPageRequestDto();
//        request.name = "Bad Group Page";
//        request.slug = "bad-group-page";
//        request.api = "https://api.devel.mon.argo.grnet.gr";
//        request.secret = "dummy";
//        request.report = "Critical";
//
//        var group = new org.grnet.status.dtos.statuspage.StatusPageGroupDto();
//        group.name = "";
//        group.alias = "Alias";
//        group.list = java.util.Collections.emptyList();
//
//        var config = new org.grnet.status.dtos.statuspage.StatusPageConfigDto();
//        config.title = "Valid title";
//        config.description = "desc";
//        config.groups = java.util.List.of(group);
//
//        var theming = new org.grnet.status.dtos.statuspage.StatusPageThemingDto();
//        theming.color = "#ffffff";
//        theming.status = new org.grnet.status.dtos.statuspage.StatusPageThemingStatusDto();
//        theming.status.icon = "led";
//        theming.status.text = "none";
//        theming.columns = "one";
//        config.theming = theming;
//
//        request.config = config;
//
//        var response = given()
//                .auth().oauth2(aliceToken)
//                .contentType(ContentType.JSON)
//                .body(request)
//                .when()
//                .post()
//                .then()
//                .statusCode(400)
//                .extract()
//                .as(InformativeResponse.class);
//
//        assertEquals(400, response.code);
//        assert response.errors.contains("group name cannot be blank");
//        assert response.errors.contains("groups list cannot be empty");
//    }


    @Test
    public void getStatusPage() {

        var request = new StatusPageRequestDto();
        request.name = "Test Page AB";
        request.slug = "test-page-" + UUID.randomUUID();
        request.api = webapi;
        request.secret = encrypt();
        request.report = "Critical";

        createTestStatusPageDto(request);

        var createStatusPage = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var getStatusPage = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .get("/{id}", createStatusPage.id)
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(StatusPageResponseDto.class);

        assertEquals(createStatusPage.name, getStatusPage.name);
    }

    @Test
    public void updateStatusPage() {

        var request = new StatusPageRequestDto();
        request.name = "Initial Page";
        request.slug = "test-page-" + UUID.randomUUID();
        request.api = webapi;
        request.secret = encrypt();
        request.report = "Critical";
        createTestStatusPageDto(request);

        var created = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        var updateRequest = new StatusPageRequestDto();
        updateRequest.name = "Updated Page";
        updateRequest.slug = created.slug;
        updateRequest.api = created.api;
        updateRequest.secret = created.secret;
        updateRequest.report = "Warning";
        createTestStatusPageDto(updateRequest);

        updateRequest.config.title = "Updated Config Title";
        updateRequest.config.description = "Updated description text";
        updateRequest.config.theming.color = "#00ff00";

        var updated = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/{id}", created.id)
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(StatusPageResponseDto.class);

        assertEquals("Updated Page", updated.name);
        assertEquals("Updated Config Title", updated.config.title);
        assertEquals("Updated description text", updated.config.description);
        assertEquals("#00ff00", updated.config.theming.color);
    }

    @Test
    public void createStatusPageWithLogoUpload() throws Exception {

        // Base64 dummy logo
        var base64Logo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/xcAAwMCAO+n7+QAAAAASUVORK5CYII=";

        var request = new StatusPageRequestDto();
        request.name = "Page With Logo";
        request.slug = "page-with-logo-" + UUID.randomUUID();
        request.api = webapi;
        request.secret = encrypt();
        request.report = "Critical";

        createTestStatusPageDto(request);
        request.config.theming.logo = base64Logo;

        var response = given()
                .auth().oauth2(aliceToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .as(StatusPageResponseDto.class);

        // Replace logo by a file URL
        var logoUrl = response.config.theming.logo;
        assert logoUrl != null && logoUrl.startsWith("http");
        assert logoUrl.contains("/logos/");

        // derive filename
        var fileName = logoUrl.substring(logoUrl.lastIndexOf('/') + 1);

        // use configured upload dir
        var filePath = java.nio.file.Paths.get(baseUploadLogoDir, fileName);
        assert java.nio.file.Files.exists(filePath) : "Expected uploaded logo file not found: " + filePath;

        java.nio.file.Files.deleteIfExists(filePath);

    }


    private void createTestStatusPageDto(StatusPageRequestDto request) {

        //  Build mock group items that exist in ARGO JSON ---
        var atland = new StatusGroupResponseDto();
        atland.name = "ATLAND";
        atland.status = "CRITICAL";

        var arnes = new StatusGroupResponseDto();
        arnes.name = "ARNES";
        arnes.status = "OK";

        var am01 = new StatusGroupResponseDto();
        am01.name = "AM-01-AANL";
        am01.status = "MISSING";

        //  Groups matching the fake API
        var group1 = new StatusPageGroupDto();
        group1.name = "group-1";
        group1.alias = "Group A";
        group1.list = List.of(atland, arnes);

        var group2 = new StatusPageGroupDto();
        group2.name = "group-2";
        group2.alias = "Group B";
        group2.list = List.of(am01);

        //  Theming
        var theming = new StatusPageThemingDto();
        theming.logo = "";
        theming.color = "#ffffff";
        theming.status = new StatusPageThemingStatusDto();
        theming.status.icon = "led";
        theming.status.text = "badge";
        theming.columns = "one";

        //  Config
        var config = new StatusPageConfigDto();
        config.title = "Test Page AB";
        config.groups = List.of(group1, group2);
        config.theming = theming;
        config.description = "add description";

        request.config = config;
    }


    private ArgoReportsResponse loadMockReports() throws Exception {
        try (var is = getClass().getClassLoader().getResourceAsStream("mocks/reports.json")) {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(is, ArgoReportsResponse.class);
        }
    }

    private ArgoStatusGroupsResponse loadMockStatusGroups() throws Exception {
        try (var is = getClass().getClassLoader().getResourceAsStream("mocks/status-groups.json")) {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(is, ArgoStatusGroupsResponse.class);
        }
    }

}
