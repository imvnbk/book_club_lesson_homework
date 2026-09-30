package client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import models.clubs.ClubRequestModel;
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

    @Step("Авторизоваться по логину и паролю")
    public static ValidatableResponse login(LoginRequestModel data) {
        return given(loginRequestSpec)
                .body(data)
                .when()
                .post("auth/token/")
                .then();
    }

    @Step("Выйти из системы")
    public static ValidatableResponse logout(LogoutRequestModel data) {
        return given(logoutRequestSpec)
                .body(data)
                .when()
                .post("auth/logout/")
                .then();
    }

    @Step("Зарегистрировать нового пользователя")
    public static ValidatableResponse register(RegistrationBodyModel data) {
        return given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register/")
                .then();
    }

    @Step("Зарегистрировать нового пользователя без слэша в конце пути")
    public static ValidatableResponse registerWithoutTrailingSlash(RegistrationBodyModel data) {
        return given(registrationRequestSpec)
                .body(data)
                .when()
                .post("users/register")
                .then();
    }

    @Step("Зарегистрировать нового пользователя с неподдерживаемым типом контента")
    public static ValidatableResponse registerWithUnsupportedMediaType(String rawBody) {
        return given(unsupportedMediaTypeRegistrationRequestSpec)
                .body(rawBody)
                .when()
                .post("users/register/")
                .then();
    }

    @Step("Обновить данные текущего пользователя")
    public static ValidatableResponse updateUser(String accessToken, UpdateUserRequestModel data) {
        return given(updateUserRequestSpec)
                .auth().oauth2(accessToken)
                .body(data)
                .when()
                .patch("users/me/")
                .then();
    }

    @Step("Получить список клубов")
    public static ValidatableResponse getClubs() {
        return given(clubsRequestSpec)
                .when()
                .get("clubs/")
                .then();
    }

    @Step("Получить список клубов, страница {page}")
    public static ValidatableResponse getClubs(int page) {
        return given(clubsRequestSpec)
                .queryParam("page", page)
                .when()
                .get("clubs/")
                .then();
    }

    @Step("Создать клуб от имени авторизованного пользователя")
    public static ValidatableResponse createClub(String accessToken, ClubRequestModel data) {
        return given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .body(data)
                .when()
                .post("clubs/")
                .then();
    }

    @Step("Создать клуб без авторизации")
    public static ValidatableResponse createClub(ClubRequestModel data) {
        return given(clubsRequestSpec)
                .body(data)
                .when()
                .post("clubs/")
                .then();
    }

    @Step("Получить клуб по id {id}")
    public static ValidatableResponse getClub(int id) {
        return given(clubsRequestSpec)
                .when()
                .get("clubs/{id}/", id)
                .then();
    }

    @Step("Полностью обновить клуб {id} (PUT)")
    public static ValidatableResponse updateClub(String accessToken, int id, ClubRequestModel data) {
        return given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .body(data)
                .when()
                .put("clubs/{id}/", id)
                .then();
    }

    @Step("Частично обновить клуб {id} (PATCH)")
    public static ValidatableResponse patchClub(String accessToken, int id, ClubRequestModel data) {
        return given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .body(data)
                .when()
                .patch("clubs/{id}/", id)
                .then();
    }

    @Step("Удалить клуб {id}")
    public static ValidatableResponse deleteClub(String accessToken, int id) {
        return given(clubsRequestSpec)
                .auth().oauth2(accessToken)
                .when()
                .delete("clubs/{id}/", id)
                .then();
    }
}
