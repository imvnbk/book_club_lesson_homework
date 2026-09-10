package tests;

import io.restassured.http.ContentType;
import models.login.LoginRequestModel;
import models.logout.LogoutRequestModel;
import models.logout.NotValidTokenResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import specs.logout.EmptyRequestModel;

import static data.TestData.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.logout.LogoutSpec.*;

public class LogoutTests extends TestBase {

    @Test
    @DisplayName("Успешный логаут из системы")
    public void successfulLogoutTest() {

        LoginRequestModel data = new LoginRequestModel(USERNAME, PASSWORD);

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

        assertThat(responseBody).isEqualTo(EMPTY_JSON_BODY);
    }

    @Test
    @DisplayName("Повторный логаут с одним и тем же refresh токеном")
    public void notValidTokenLogoutTest() {

        LoginRequestModel data =
                new LoginRequestModel(USERNAME, PASSWORD);

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
                .isEqualTo(TOKEN_BLACKLISTED_ERROR);

        assertThat(responseBody.code())
                .isEqualTo(TOKEN_NOT_VALID_CODE);
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
                .containsExactly(BLANK_FIELD_ERROR);
    }
}
