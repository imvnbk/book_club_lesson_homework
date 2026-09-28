package tests;

import client.ApiClient;
import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.user.UpdateUserRequestModel;
import models.user.UpdateUserResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static data.TestData.*;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.user.UpdateUserSpec.successfulUpdateUserResponseSpec;

public class UpdateUserTests extends TestBase {

    private String firstName;
    private String lastName;
    private String email;

    @BeforeEach
    public void prepareTestData() {
        firstName = randomFirstName();
        lastName = randomLastName();
        email = randomEmail();
    }

    @Test
    @DisplayName("Успешное обновление имени, фамилии и email")
    public void successfulUpdateUserTest() {

        LoginRequestModel data =
                new LoginRequestModel(USERNAME, PASSWORD);

        SuccessfulLoginResponseModel loginResponse = step("Авторизоваться и получить access токен", () ->
                ApiClient.login(data)
                        .spec(successfulLoginResponseSpec)
                        .extract()
                        .as(SuccessfulLoginResponseModel.class));

        UpdateUserRequestModel updateUserData =
                new UpdateUserRequestModel(
                        firstName,
                        lastName,
                        email
                );

        UpdateUserResponseModel response = step("Обновить профиль пользователя новыми данными", () ->
                ApiClient.updateUser(loginResponse.access(), updateUserData)
                        .spec(successfulUpdateUserResponseSpec)
                        .extract()
                        .as(UpdateUserResponseModel.class));

        step("Проверить, что имя, фамилия и email обновились", () -> {
            assertThat(response.firstName()).isEqualTo(firstName);
            assertThat(response.lastName()).isEqualTo(lastName);
            assertThat(response.email()).isEqualTo(email);
        });
    }
}