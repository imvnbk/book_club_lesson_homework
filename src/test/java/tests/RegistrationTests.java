package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.registration.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static data.TestData.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.registration.RegistrationSpec.*;

public class RegistrationTests extends TestBase{

    String username;
    String password;

    @BeforeEach
    public void prepareTestData(){
        username = randomFirstName();
        password = randomFirstName();
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
                .matches(IP_ADDRESS_REGEX);
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
                .contains(REGISTER_LOCATION_PATH);
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
                .spec(unsupportedMediaTypeRegistrationResponseSpec)
                .extract()
                .as(UnsupportedMediaTypeResponseModel.class);

        assertThat(UNSUPPORTED_MEDIA_TYPE_ERROR).isEqualTo(response.detail());
    }

    @Test
    @DisplayName("Ошибка 400 при регистрации с невалидным username")
    public void invalidUsername400Test() {

        RegistrationBodyModel data =
                new RegistrationBodyModel(INVALID_USERNAME, password);

        String actualError = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register/")
                .then()
                .spec(invalidUsernameRegistrationResponseSpec)
                .extract()
                .path("username[0]");

        assertThat(actualError)
                .isEqualTo(INVALID_USERNAME_ERROR);
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

        assertThat(EXISTING_USER_ERROR).isEqualTo(response.username().get(0));
    }
}
