package tests;

import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;

public class UpdateUserTests extends TestBase {

    String username = "qaguru";
    String password = "qaguru123";

    @Test
    @DisplayName("Успешное обновление имени, фамилии и email")
    public void successfulUpdateUserTest() {

        LoginRequestModel data = new LoginRequestModel(username, password);

        SuccessfulLoginResponseModel loginResponse = given(loginRequestSpec)
                .body(data)
                .when()
                .post("auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().as(SuccessfulLoginResponseModel.class);
    }
}
