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

    public static ResponseSpecification successfulClubCreationResponseSpec = baseResponseSpec(201)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/clubs/club_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("owner", notNullValue())
            .build();

    public static ResponseSpecification successfulClubResponseSpec = baseResponseSpec(200)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/clubs/club_response_schema.json"))
            .expectBody("id", notNullValue())
            .build();

    public static ResponseSpecification clubUnauthorizedResponseSpec = baseResponseSpec(401)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/clubs/club_unauthorized_response_schema.json"))
            .expectBody("detail", notNullValue())
            .build();

    public static ResponseSpecification clubDeletedResponseSpec = baseResponseSpec(204)
            .build();

    public static ResponseSpecification existingBookTitleClubResponseSpec = baseResponseSpec(400)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/clubs/existing_book_title_response_schema.json"))
            .build();
}
