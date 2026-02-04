package org.grnet.status.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.grnet.status.api.endpoints.UserEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.user.UserProfileDto;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(UserEndpoint.class)
public class UserEndpointTest extends KeycloakTest {

    @Test
    public void getUserProfile() {

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

        assertEquals("LOCALTENANT", group.name);
        assertEquals("viewer", group.role);
    }

    @Test
    public void registerUser() {
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
