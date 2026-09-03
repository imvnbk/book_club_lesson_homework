package tests;

import io.restassured.http.ContentType;
import models.registration.ExistingUserResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.RegistrationResponseModel;
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
    public void negativeRegistration500Test() {
        
        String data = "{\"username\": \"" + username + "\",\n" +
                "  \"password\": \"" + password + "\"}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register")
                .then()
                .log().all()
                .statusCode(500)
                .body("username", is(username))
                .body("id", notNullValue());

    }

    @Test
    public void unsupportedMediaType415Test() {
        
        String data = "{\"username\": \"" + username + "\",\n" +
                "  \"password\": \"" + password + "\"}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register/")
                .then()
                .log().all()
                .statusCode(415)
                .body("username", contains("Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters."));
    }

    @Test
    public void invalidUsername400Test() {
        
        String data = "{\"username\": \"" + username + "\",\n" +
                "  \"password\": \"" + password + "\"}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("https://book-club.qa.guru/api/v1/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .body("username", contains("Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters."));
    }

    @Test
    public void existingUser400Test() {

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
