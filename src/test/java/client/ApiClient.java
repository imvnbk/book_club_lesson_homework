package client;

import io.restassured.response.ValidatableResponse;
import models.login.LoginRequestModel;
import models.logout.LogoutRequestModel;
import models.registration.RegistrationBodyModel;
import models.user.UpdateUserRequestModel;

import static io.restassured.RestAssured.given;
import static specs.clubs.ClubsSpec.clubsRequestSpec;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.logout.LogoutSpec.logoutRequestSpec;
import static specs.registration.RegistrationSpec.registrationRequestSpec;
import static specs.registration.RegistrationSpec.unsupportedMediaTypeRegistrationRequestSpec;
import static specs.user.UpdateUserSpec.updateUserRequestSpec;

public class ApiClient {

    public static ValidatableResponse login(LoginRequestModel data) {
        return given(loginRequestSpec)
                .body(data)
                .when()
                .post("auth/token/")
                .then();
    }

    public static ValidatableResponse logout(LogoutRequestModel data) {
        return given(logoutRequestSpec)
                .body(data)
                .when()
                .post("auth/logout/")
                .then();
    }

    public static ValidatableResponse register(RegistrationBodyModel data) {
        return given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register/")
                .then();
    }

    public static ValidatableResponse registerWithoutTrailingSlash(RegistrationBodyModel data) {
        return given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register")
                .then();
    }

    public static ValidatableResponse registerWithUnsupportedMediaType(String rawBody) {
        return given(unsupportedMediaTypeRegistrationRequestSpec)
                .body(rawBody)
                .when()
                .post("users/register/")
                .then();
    }

    public static ValidatableResponse updateUser(String accessToken, UpdateUserRequestModel data) {
        return given(updateUserRequestSpec)
                .auth().oauth2(accessToken)
                .body(data)
                .when()
                .patch("users/me/")
                .then();
    }

    public static ValidatableResponse getClubs() {
        return given(clubsRequestSpec)
                .when()
                .get("clubs/")
                .then();
    }

    public static ValidatableResponse getClubs(int page) {
        return given(clubsRequestSpec)
                .queryParam("page", page)
                .when()
                .get("clubs/")
                .then();
    }
}
