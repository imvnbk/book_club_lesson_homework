package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import models.registration.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationTests {

    String username;
    String password;

    @BeforeEach
    public void prepareTestData(){
        Faker faker = new Faker();

        username = faker.name().firstName();
        password = faker.name().firstName();
    }

    @Test
    public void successfulRegistrationTest() {

        RegistrationBodyModel data = new RegistrationBodyModel(username, password);

        RegistrationResponseModel registrationResponse = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register/")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(RegistrationResponseModel.class);

        assertEquals(username, registrationResponse.username());

    }

    @Test
    public void registrationWithoutTrailingSlash301Test() {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register")
                .then()
                .log().all()
                .statusCode(301);
    }

    @Test
    public void unsupportedMediaType415Test() throws JsonProcessingException {

        RegistrationBodyModel data =
                new RegistrationBodyModel(username, password);

        String body = new ObjectMapper().writeValueAsString(data);

        UnsupportedMediaTypeResponseModel response = given()
                .log().all()
                .contentType(ContentType.TEXT)
                .body(body)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register/")
                .then()
                .log().all()
                .statusCode(415)
                .extract()
                .as(UnsupportedMediaTypeResponseModel.class);

        String expectedError =
                "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";

        assertEquals(expectedError, response.detail());
    }

    @Test
    public void invalidUsername400Test() {

        String invalidUsername = "invalid username";

        RegistrationBodyModel data =
                new RegistrationBodyModel(invalidUsername, password);

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .body(
                        "username",
                        contains("Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters.")
                );
    }

    @Test
    public void existingUser400Test() {

        RegistrationBodyModel data = new RegistrationBodyModel(username, password);

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register/")
                .then()
                .log().all()
                .statusCode(201);

        ExistingUserResponseModel response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(ExistingUserResponseModel.class);
                String expectedError =  "A user with that username already exists.";
                assertEquals(expectedError, response.username().get(0));

    }
}
