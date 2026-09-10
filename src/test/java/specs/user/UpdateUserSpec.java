package specs.user;

import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class UpdateUserSpec {
    public static RequestSpecification updateUserRequestSpec = with()
            .log().all()
            .contentType(ContentType.JSON)
            .basePath("api/v1/");
    public static ResponseSpecification successfulUpdateUserResponseSpec = new ResponseSpecBuilder()
                    .log(ALL)
                    .expectStatusCode(200)
                    .expectBody(matchesJsonSchemaInClasspath(
                            "schemas/user/successful_update_user_response_schema.json"))
                    .build();

}
