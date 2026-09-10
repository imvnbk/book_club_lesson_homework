package specs.logout;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;
import static specs.BaseSpec.baseResponseSpec;

public class LogoutSpec {

    public static RequestSpecification logoutRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulLogoutResponseSpec = baseResponseSpec(200)
            .build();

    public static ResponseSpecification notValidTokenResponseSpec = baseResponseSpec(401)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/logout/token_not_valid_logout_response_schema.json"))
            .expectBody("detail", notNullValue())
            .expectBody("code", notNullValue())
            .build();

    public static ResponseSpecification emptyBodyResponseSpec = baseResponseSpec(400)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/logout/empty_logout_response_schema.json"))
            .expectBody("refresh", notNullValue())
            .build();
}
