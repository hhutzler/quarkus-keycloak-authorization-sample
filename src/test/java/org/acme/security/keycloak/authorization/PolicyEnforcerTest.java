package org.acme.security.keycloak.authorization;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.keycloak.representations.AccessTokenResponse;
import java.util.HashMap;
import java.util.Map;

@QuarkusTest
public class PolicyEnforcerTest {
    static {
        RestAssured.useRelaxedHTTPSValidation();
    }

    private static final String KEYCLOAK_SERVER_URL = System.getProperty("keycloak.url", "http://localhost:8280/auth");
    private static final String KEYCLOAK_REALM = "RBAC";
    private static final String CLIENT_ID = "app-client";
    private static final String CLIENT_SECRET= "0a32b2ad-7b58-4c5b-bffe-7d3673fe70a3";

    // Get an Access Tone
    private String getAccessToken(String userName, String pwd) {
        return RestAssured
                .given()
                .param("grant_type", "password")
                .param("username", userName)
                .param("password", pwd)
                .param("client_id", CLIENT_ID)
                .param("client_secret", CLIENT_SECRET)
                .when()
                .post(KEYCLOAK_SERVER_URL + "/realms/" + KEYCLOAK_REALM + "/protocol/openid-connect/token")
                .as(AccessTokenResponse.class).getToken();
    }

    @Test
    public void testUserCanViewAccountResource() {
         RestAssured.given().auth().oauth2(getAccessToken("testuser" ,"xxx"))
                 .when().get("/accounts")
                 .then()
                 .statusCode(200);
     }

    public void testAdminCanViewAccountResource() {
        RestAssured.given().auth().oauth2(getAccessToken("testadmin" ,"xxx"))
                .when().get("/accounts")
                .then()
                .statusCode(200);
    }

    @Test
    public void testWrongAccessToken() {
        RestAssured.given().auth().oauth2("1234")
                .when().get("/accounts")
                .then()
                .statusCode(401);
    }

    @Test
    public void testUserCanCreateAccountResource() {
        Map<String, Object> map = new HashMap<>();
        map.put("accountname", "Test12");
        RestAssured.given().auth().oauth2(getAccessToken("testuser" ,"xxx"))
                .body(map)
                .when().post("/accounts")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAdminCanCreateAccountResource() {
        Map<String, Object> map = new HashMap<>();
        map.put("accountname", "Test12");
        RestAssured.given().auth().oauth2(getAccessToken("testadmin" ,"xxx"))
                .body(map)
                .when().post("/accounts")
                .then()
                .statusCode(200);
    }

    @Test
    public void testPublicResource() {
        RestAssured.given()
                .when().get("/api/public")
                .then()
                .statusCode(204);
    }
}
