package tests;

import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.user.UpdateUserRequestModel;
import models.user.UpdateUserResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.user.UpdateUserSpec.successfulUpdateUserResponseSpec;
import static specs.user.UpdateUserSpec.updateUserRequestSpec;

public class UpdateUserTests extends TestBase {

    private final String username = "qaguru";
    private final String password = "qaguru123";
    private String firstName;
    private String lastName;
    private String email;

    @BeforeEach
    public void prepareTestData() {
        Faker faker = new Faker();

        firstName = faker.name().firstName();
        lastName = faker.name().lastName();
        email = faker.internet().emailAddress();
    }

    @Test
    @DisplayName("Успешное обновление имени, фамилии и email")
    public void successfulUpdateUserTest() {

        LoginRequestModel data =
                new LoginRequestModel(username, password);

        SuccessfulLoginResponseModel loginResponse = given(loginRequestSpec)
                .body(data)
                .when()
                .post("auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class);

        UpdateUserRequestModel updateUserData =
                new UpdateUserRequestModel(
                        firstName,
                        lastName,
                        email
                );

        UpdateUserResponseModel response = given(updateUserRequestSpec)
                .auth()
                .oauth2(loginResponse.access())
                .body(updateUserData)
                .when()
                .patch("users/me/")
                .then()
                .spec(successfulUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserResponseModel.class);

        assertThat(response.firstName()).isEqualTo(firstName);
        assertThat(response.lastName()).isEqualTo(lastName);
        assertThat(response.email()).isEqualTo(email);
    }
}