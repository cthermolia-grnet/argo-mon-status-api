package org.grnet.status.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import org.grnet.status.services.TenantService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.net.URI;

import io.quarkus.test.common.http.TestHTTPResource;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KeycloakTest {

    @Inject
    TenantService tenantService;
    @TestHTTPResource
    URI baseUri;
    protected String adminToken;
    protected String aliceToken;
    protected String bobToken;

    protected String tenantAdmin;

    protected String tenantViewer;


    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @BeforeAll
    public void setup() {
        RestAssured.baseURI = baseUri.toString();
        adminToken = getAccessToken("admin");
        aliceToken = getAccessToken("alice");
        bobToken = getAccessToken("bob");
        tenantAdmin = getAccessToken("tenantadmin");
        tenantViewer = getAccessToken("tenantviewer");
        tenantService.deleteAll();
    }

    protected String getAccessToken(String username) {
        return keycloakClient.getAccessToken(username);
    }
}
