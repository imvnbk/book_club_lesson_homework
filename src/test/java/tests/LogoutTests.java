package tests;

import io.restassured.http.ContentType;
import models.login.LoginRequestModel;
import models.logout.LogoutRequestModel;
import models.logout.NotValidTokenResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import specs.logout.EmptyRequestModel;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.logout.LogoutSpec.*;

public class LogoutTests extends TestBase {

    String username = "qaguru";
    String password = "qaguru123";

    @Test
    @DisplayName("Успешный логаут из системы")
    public void successfulLogoutTest() {

        LoginRequestModel data = new LoginRequestModel(username, password);

        String refreshToken = given(loginRequestSpec)
                .body(data)
                .when()
                .post("auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract()
                .path("refresh");

        LogoutRequestModel logoutData =
                new LogoutRequestModel(refreshToken);

        String responseBody = given()
                .log().all()
                .contentType(ContentType.JSON)
                .basePath("api/v1/")
                .body(logoutData)
                .when()
                .post("auth/logout/")
                .then()
                .spec(successfulLogoutResponseSpec)
                .extract()
                .asString();

        assertThat(responseBody).isEqualTo("{}");
    }

    @Test
    @DisplayName("Повторный логаут с одним и тем же refresh токеном")
    public void notValidTokenLogoutTest() {

        LoginRequestModel data =
                new LoginRequestModel(username, password);

        String refreshToken = given(loginRequestSpec)
                .body(data)
                .when()
                .post("auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract()
                .path("refresh");

        LogoutRequestModel logoutData =
                new LogoutRequestModel(refreshToken);

        given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("auth/logout/")
                .then()
                .statusCode(200);

        NotValidTokenResponseModel responseBody = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("auth/logout/")
                .then()
                .spec(notValidTokenResponseSpec)
                .extract().as(NotValidTokenResponseModel.class);

        assertThat(responseBody.detail())
                .isEqualTo("Token is blacklisted");

        assertThat(responseBody.code())
                .isEqualTo("token_not_valid");
    }

    @Test
    @DisplayName("Отправка пустого значения в refresh токен")
    public void logoutWithEmptyRefreshTest() {

        LogoutRequestModel logoutData =
                new LogoutRequestModel("");

        EmptyRequestModel responseBody = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("auth/logout/")
                .then()
                .spec(emptyBodyResponseSpec)
                .extract()
                .as(EmptyRequestModel.class);

        assertThat(responseBody.refresh())
                .containsExactly("This field may not be blank.");
    }
}
