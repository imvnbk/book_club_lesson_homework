package specs.registration;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;
import static specs.BaseSpec.baseResponseSpec;

public class RegistrationSpec {

    public static RequestSpecification registrationRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulRegistrationResponseSpec = baseResponseSpec(201)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/registration/successful_registration_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("username", notNullValue())
            .expectBody("remoteAddr", notNullValue())
            .build();

    public static ResponseSpecification withoutTrailingSlashRegistrationResponseSpec = baseResponseSpec(301)
            .expectHeader("Location", notNullValue(String.class))
            .build();

    public static RequestSpecification unsupportedMediaTypeRegistrationRequestSpec = with()
            .log().all()
            .contentType(ContentType.TEXT)
            .basePath("api/v1/");

    public static ResponseSpecification unsupportedMediaTypeRegistrationResponseSpec = baseResponseSpec(415)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/registration/unsupported_media_type_registration_response_schema.json"))
            .build();

    public static ResponseSpecification invalidUsernameRegistrationResponseSpec = baseResponseSpec(400)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/registration/invalid_username_registration_response_schema.json"))
            .build();

    public static ResponseSpecification existingUserRegistrationResponseSpec = baseResponseSpec(400)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/registration/existing_user_registration_response_schema.json"))
            .build();
}
