package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.registration.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.registration.RegistrationSpec.*;

public class RegistrationTests extends TestBase{

    String username;
    String password;

    @BeforeEach
    public void prepareTestData(){
        Faker faker = new Faker();

        username = faker.name().firstName();
        password = faker.name().firstName();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя с валидными данными")
    public void successfulRegistrationTest() {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        RegistrationResponseModel registrationResponse = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(RegistrationResponseModel.class);

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
                .matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    @Test
    @DisplayName("Редирект 301 при регистрации без завершающего слеша в URL")
    public void registrationWithoutTrailingSlash301Test() {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        String location = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register")
                .then()
                .spec(withoutTrailingSlashRegistrationResponseSpec)
                .extract()
                .header("Location");

        assertThat(location)
                .contains("/api/v1/users/register/");
    }

    @Test
    @DisplayName("Ошибка 415 при отправке запроса с неподдерживаемым Content-Type")
    public void unsupportedMediaType415Test() throws JsonProcessingException {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        String body = new ObjectMapper().writeValueAsString(data);

        UnsupportedMediaTypeResponseModel response = given(unsupportedMediaTypeRegistrationRequestSpec)
                .body(body)
                .when()
                .post("users/register/")
                .then()
                .log().all()
                .spec(unsupportedMediaTypeRegistrationResponseSpec)
                .extract()
                .as(UnsupportedMediaTypeResponseModel.class);

        String expectedError =
                "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";

        assertThat(expectedError).isEqualTo(response.detail());
    }

    @Test
    @DisplayName("Ошибка 400 при регистрации с невалидным username")
    public void invalidUsername400Test() {

        String invalidUsername = "invalid username";

        RegistrationBodyModel data =
                new RegistrationBodyModel(invalidUsername, password);

        String actualError = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register/")
                .then()
                .spec(invalidUsernameRegistrationResponseSpec)
                .extract()
                .path("username[0]");

        assertThat(actualError)
                .isEqualTo(
                        "Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters."
                );
    }

    @Test
    @DisplayName("Ошибка 400 при регистрации уже существующего пользователя")
    public void existingUser400Test() {

        RegistrationBodyModel data = new RegistrationBodyModel(username, password);

        given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        ExistingUserResponseModel response = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register/")
                .then()
                .spec(existingUserRegistrationResponseSpec)
                .extract()
                .as(ExistingUserResponseModel.class);

        String expectedError =  "A user with that username already exists.";
                assertThat(expectedError).isEqualTo(response.username().get(0));
    }
}
