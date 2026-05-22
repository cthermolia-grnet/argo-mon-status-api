package org.grnet.status.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpointRepository;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.status.api.endpoints.UserEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.services.GroupManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
@TestHTTPEndpoint(UserEndpoint.class)
public class UserEndpointTest extends KeycloakTest {

    @Inject
    TestEntitlementProvider entitlementProvider;

    @Inject
    RoleEndpointRepository roleEndpointRepository;
    @InjectMock
    GroupManagementService groupManagementService;
    // -------------------------------------------------------------------------
    // SETUP ROLE REPOSITORY
    // -------------------------------------------------------------------------
    @BeforeEach
    void setupRepo() {
        TestRoleEndpointRepository testRepo = new TestRoleEndpointRepository();
        QuarkusMock.installMockForType(testRepo, RoleEndpointRepository.class);
        this.roleEndpointRepository = testRepo;
    }

    // -------------------------------------------------------------------------
    // RESET STATE
    // -------------------------------------------------------------------------
    @BeforeEach
    void reset() {
        entitlementProvider.reset();
        ((TestRoleEndpointRepository) roleEndpointRepository).reset();
    }

    // -------------------------------------------------------------------------
    // ENTITLEMENTS
    // -------------------------------------------------------------------------
    private void mockSuperAdmin() {
        entitlementProvider.setSuperAdmin(true);
        entitlementProvider.setEntitlements(List.of());
    }

    private void mockTenantViewer() {
        entitlementProvider.setSuperAdmin(false);
        entitlementProvider.setEntitlements(List.of(
                entitlement("members")
        ));
    }


    private Entitlement entitlement(String role) {
        String raw = "urn:mace:grnet.gr:einfra:login-devel:group:status-pages:"
                + role + ":role=member";

        return new Entitlement(
                "status-pages",
                List.of("status-pages", role),
                role,
                raw
        );
    }

    @Test
    public void getUserProfile() {

        mockTenantViewer();

        ((TestRoleEndpointRepository) roleEndpointRepository).set(List.of( new RoleEndpoint( 1L, "members", "members", "GET_/v1/profile", LocalDateTime.now(), null)));

        var response = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .get("/profile")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(UserProfileDto.class);

        assertEquals("local_viewer_voperson_id", response.username);

        var group = response.groups.get(0);

        assertEquals("members", group.name);
        assertEquals("members", group.role);
    }

    @Test
    public void registerUser() {
        mockTenantViewer();
        Mockito.doNothing()
                .when(groupManagementService)
                .addMember(anyString(), anyString());

        var response = given()
                .auth().oauth2(tenantViewer)
                .contentType(ContentType.JSON)
                .post("/registration")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(InformativeResponse.class);

        assertEquals(response.message, "Registration completed.");
    }
}
