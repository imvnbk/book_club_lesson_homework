package tests;

import io.restassured.http.ContentType;
import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.login.WrongCredentialsLoginResponseModel;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class LoginTests extends TestBase {

    String username = "qaguru";
    String password = "qaguru123";
    String wrongPassword = "wrongPassword";

    @Test
    public void successfulLoginTest() {

        LoginRequestModel data = new LoginRequestModel(username, password);

        SuccessfulLoginResponseModel loginResponse = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .basePath("api/v1/")
                .post("auth/token/")
                .then()
                .log().all()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(
                        "schemas/successful_login_response_schema.json"))
                .body("refresh", notNullValue())
                .body("access", notNullValue())
                .extract().as(SuccessfulLoginResponseModel.class);

        String expectedTokenPath = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

        assertThat(actualAccess).startsWith(expectedTokenPath);
        assertThat(actualRefresh).startsWith(expectedTokenPath);
        assertThat(actualAccess).isNotEqualTo(actualRefresh);
    }

    @Test
    public void wrongCredentialsTest() {

        LoginRequestModel data = new LoginRequestModel(username, wrongPassword);

        WrongCredentialsLoginResponseModel loginResponse = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .basePath("api/v1/")
                .post("auth/token/")
                .then()
                .log().all()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath(
                        "schemas/wrong_credentials_login_response_schema.json"))
                .body("detail", notNullValue())
                .extract().as(WrongCredentialsLoginResponseModel.class);

        String expectedDetailError = "Invalid username or password.";
        String actualDetailError = loginResponse.detail();

        assertThat(actualDetailError).isEqualTo(expectedDetailError);
    }
}
