package specs.reviews;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestSpec;
import static specs.BaseSpec.baseResponseSpec;

public class ReviewsSpec {

    public static RequestSpecification reviewsRequestSpec = baseRequestSpec;

    public static ResponseSpecification successfulReviewsResponseSpec = baseResponseSpec(200)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/reviews/get_reviews_response_schema.json"))
            .expectBody("count", notNullValue())
            .expectBody("results", notNullValue())
            .build();

    public static ResponseSpecification successfulReviewCreationResponseSpec = baseResponseSpec(201)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/reviews/review_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("user", notNullValue())
            .build();

    public static ResponseSpecification successfulReviewResponseSpec = baseResponseSpec(200)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/reviews/review_response_schema.json"))
            .expectBody("id", notNullValue())
            .build();

    public static ResponseSpecification reviewDeletedResponseSpec = baseResponseSpec(204)
            .build();

    public static ResponseSpecification reviewUnauthorizedResponseSpec = baseResponseSpec(401)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/reviews/review_error_response_schema.json"))
            .build();

    public static ResponseSpecification reviewForbiddenResponseSpec = baseResponseSpec(403)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/reviews/review_error_response_schema.json"))
            .build();

    public static ResponseSpecification reviewNotFoundResponseSpec = baseResponseSpec(404)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/reviews/review_error_response_schema.json"))
            .build();

    public static ResponseSpecification reviewValidationErrorResponseSpec = baseResponseSpec(400)
            .build();
}
