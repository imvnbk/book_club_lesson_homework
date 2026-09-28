package tests;

import client.ApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.registration.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static data.TestData.*;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.registration.RegistrationSpec.*;

public class RegistrationTests extends TestBase{

    String username;
    String password;

    @BeforeEach
    public void prepareTestData(){
        username = randomUsername();
        password = randomFirstName();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя с валидными данными")
    public void successfulRegistrationTest() {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        RegistrationResponseModel registrationResponse = step("Зарегистрировать нового пользователя с валидными данными", () ->
                ApiClient.register(data)
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(RegistrationResponseModel.class));

        step("Проверить данные зарегистрированного пользователя в ответе", () -> {
            assertThat(registrationResponse.username())
                    .isEqualTo(username);

            assertThat(registrationResponse.firstName())
                    .isEqualTo("");

            assertThat(registrationResponse.lastName())
                    .isEqualTo("");

            assertThat(registrationResponse.email())
                    .isEqualTo("");

            assertThat(registrationResponse.remoteAddr())
                    .isNotNull()
                    .isNotBlank()
                    .matches(IP_ADDRESS_REGEX);
        });
    }

    @Test
    @DisplayName("Редирект 301 при регистрации без завершающего слеша в URL")
    public void registrationWithoutTrailingSlash301Test() {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        String location = step("Отправить запрос регистрации без завершающего слеша в URL", () ->
                ApiClient.registerWithoutTrailingSlash(data)
                        .spec(withoutTrailingSlashRegistrationResponseSpec)
                        .extract()
                        .header("Location"));

        step("Проверить, что заголовок Location содержит корректный путь", () ->
                assertThat(location)
                        .contains(REGISTER_LOCATION_PATH));
    }

    @Test
    @DisplayName("Ошибка 415 при отправке запроса с неподдерживаемым Content-Type")
    public void unsupportedMediaType415Test() throws JsonProcessingException {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        String body = step("Сериализовать тело запроса в JSON", () ->
                new ObjectMapper().writeValueAsString(data));

        UnsupportedMediaTypeResponseModel response = step("Отправить запрос регистрации с неподдерживаемым Content-Type", () ->
                ApiClient.registerWithUnsupportedMediaType(body)
                        .spec(unsupportedMediaTypeRegistrationResponseSpec)
                        .extract()
                        .as(UnsupportedMediaTypeResponseModel.class));

        step("Проверить сообщение об ошибке 415", () ->
                assertThat(UNSUPPORTED_MEDIA_TYPE_ERROR).isEqualTo(response.detail()));
    }

    @Test
    @DisplayName("Ошибка 400 при регистрации с невалидным username")
    public void invalidUsername400Test() {

        RegistrationBodyModel data =
                new RegistrationBodyModel(INVALID_USERNAME, password);

        String actualError = step("Отправить запрос регистрации с невалидным username", () ->
                ApiClient.register(data)
                        .spec(invalidUsernameRegistrationResponseSpec)
                        .extract()
                        .path("username[0]"));

        step("Проверить текст ошибки валидации username", () ->
                assertThat(actualError)
                        .isEqualTo(INVALID_USERNAME_ERROR));
    }

    @Test
    @DisplayName("Ошибка 400 при регистрации уже существующего пользователя")
    public void existingUser400Test() {

        RegistrationBodyModel data = new RegistrationBodyModel(username, password);

        step("Зарегистрировать пользователя первый раз", () ->
                ApiClient.register(data)
                        .spec(successfulRegistrationResponseSpec));

        ExistingUserResponseModel response = step("Повторно зарегистрировать того же пользователя", () ->
                ApiClient.register(data)
                        .spec(existingUserRegistrationResponseSpec)
                        .extract()
                        .as(ExistingUserResponseModel.class));

        step("Проверить сообщение об ошибке уже существующего пользователя", () ->
                assertThat(EXISTING_USER_ERROR).isEqualTo(response.username().get(0)));
    }
}
