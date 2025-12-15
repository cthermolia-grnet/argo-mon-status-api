package org.grnet.status.api;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.grnet.status.api.endpoints.UserEndpoint;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.services.UserService;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(UserEndpoint.class)
public class UserEndpointTest extends KeycloakTest {


    @Inject
    UserService userService;

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

        assertEquals("tenantviewer", response.username);
    }
}
