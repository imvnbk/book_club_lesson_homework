package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;

public class BaseSpec {

    public static RequestSpecification baseRequestSpec = with()
            .log().all()
            .contentType(ContentType.JSON)
            .basePath("api/v1/");

    public static ResponseSpecBuilder baseResponseSpec(int statusCode) {
        return new ResponseSpecBuilder()
                .log(ALL)
                .expectStatusCode(statusCode);
    }
}
