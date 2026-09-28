package tests;

import client.ApiClient;
import models.login.LoginRequestModel;
import models.logout.LogoutRequestModel;
import models.logout.NotValidTokenResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import specs.logout.EmptyRequestModel;

import static data.TestData.*;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.logout.LogoutSpec.*;

public class LogoutTests extends TestBase {

    @Test
    @DisplayName("Успешный логаут из системы")
    public void successfulLogoutTest() {

        LoginRequestModel data = new LoginRequestModel(USERNAME, PASSWORD);

        String refreshToken = step("Авторизоваться и получить refresh токен", () ->
                ApiClient.login(data)
                        .spec(successfulLoginResponseSpec)
                        .extract()
                        .path("refresh"));

        LogoutRequestModel logoutData =
                new LogoutRequestModel(refreshToken);

        String responseBody = step("Выполнить логаут с полученным refresh токеном", () ->
                ApiClient.logout(logoutData)
                        .spec(successfulLogoutResponseSpec)
                        .extract()
                        .asString());

        step("Проверить, что тело ответа пустое", () ->
                assertThat(responseBody).isEqualTo(EMPTY_JSON_BODY));
    }

    @Test
    @DisplayName("Повторный логаут с одним и тем же refresh токеном")
    public void notValidTokenLogoutTest() {

        LoginRequestModel data =
                new LoginRequestModel(USERNAME, PASSWORD);

        String refreshToken = step("Авторизоваться и получить refresh токен", () ->
                ApiClient.login(data)
                        .spec(successfulLoginResponseSpec)
                        .extract()
                        .path("refresh"));

        LogoutRequestModel logoutData =
                new LogoutRequestModel(refreshToken);

        step("Выполнить первый логаут с refresh токеном", () ->
                ApiClient.logout(logoutData)
                        .statusCode(200));

        NotValidTokenResponseModel responseBody = step("Повторно отправить логаут с уже использованным токеном", () ->
                ApiClient.logout(logoutData)
                        .spec(notValidTokenResponseSpec)
                        .extract().as(NotValidTokenResponseModel.class));

        step("Проверить сообщение и код ошибки о невалидном токене", () -> {
            assertThat(responseBody.detail())
                    .isEqualTo(TOKEN_BLACKLISTED_ERROR);

            assertThat(responseBody.code())
                    .isEqualTo(TOKEN_NOT_VALID_CODE);
        });
    }

    @Test
    @DisplayName("Отправка пустого значения в refresh токен")
    public void logoutWithEmptyRefreshTest() {

        LogoutRequestModel logoutData =
                new LogoutRequestModel("");

        EmptyRequestModel responseBody = step("Отправить логаут с пустым refresh токеном", () ->
                ApiClient.logout(logoutData)
                        .spec(emptyBodyResponseSpec)
                        .extract()
                        .as(EmptyRequestModel.class));

        step("Проверить сообщение об ошибке валидации пустого поля", () ->
                assertThat(responseBody.refresh())
                        .containsExactly(BLANK_FIELD_ERROR));
    }
}
