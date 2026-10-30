package tests;

import client.ApiClient;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.SignInPage;

import static data.TestData.PASSWORD;
import static data.TestData.randomUsername;
import static io.qameta.allure.Allure.step;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;

@Tag("ui")
public class UiLoginTests extends UiTestBase {

    @Test
    @DisplayName("Успешный вход через форму авторизации")
    public void signInViaFormTest() {

        String username = randomUsername();

        step("Зарегистрировать нового пользователя через API", () ->
                ApiClient.register(new RegistrationBodyModel(username, PASSWORD))
                        .spec(successfulRegistrationResponseSpec));

        new SignInPage().open().signIn(username, PASSWORD);
    }
}
