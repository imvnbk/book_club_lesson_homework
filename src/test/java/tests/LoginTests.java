package tests;

import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.login.WrongCredentialsLoginResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static data.TestData.*;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.*;

public class LoginTests extends TestBase {

    @Test
    @DisplayName("Успешная авторизация с валидными учетными данными")
    public void successfulLoginTest() {

        LoginRequestModel data = new LoginRequestModel(USERNAME, PASSWORD);

        SuccessfulLoginResponseModel loginResponse = step("Отправить запрос авторизации с валидными учетными данными", () ->
                given(loginRequestSpec)
                        .body(data)
                        .when()
                        .post("auth/token/")
                        .then()
                        .spec(successfulLoginResponseSpec)
                        .extract().as(SuccessfulLoginResponseModel.class));

        step("Проверить, что access и refresh токены валидны", () -> {
            String actualAccess = loginResponse.access();
            String actualRefresh = loginResponse.refresh();

            assertThat(actualAccess).startsWith(JWT_PREFIX);
            assertThat(actualRefresh).startsWith(JWT_PREFIX);
            assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });
    }

    @Test
    @DisplayName("Ошибка авторизации с неверным паролем")
    public void wrongCredentialsTest() {

        LoginRequestModel data = new LoginRequestModel(USERNAME, WRONG_PASSWORD);

        WrongCredentialsLoginResponseModel loginResponse = step("Отправить запрос авторизации с неверным паролем", () ->
                given(loginRequestSpec)
                        .body(data)
                        .when()
                        .post("auth/token/")
                        .then()
                        .spec(wrongCredentialsLoginResponseSpec)
                        .extract().as(WrongCredentialsLoginResponseModel.class));

        step("Проверить текст ошибки о неверных учетных данных", () -> {
            String actualDetailError = loginResponse.detail();

            assertThat(actualDetailError).isEqualTo(INVALID_CREDENTIALS_ERROR);
        });
    }
}
