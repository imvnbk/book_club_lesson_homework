package specs.clubs;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;
import static specs.BaseSpec.baseResponseSpec;

public class ClubsSpec {

    public static RequestSpecification clubsRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulClubsResponseSpec = baseResponseSpec(200)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/clubs/get_clubs_response_schema.json"))
            .expectBody("count", notNullValue())
            .expectBody("results", notNullValue())
            .build();
}
