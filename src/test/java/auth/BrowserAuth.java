package auth;

import client.ApiClient;
import io.qameta.allure.Step;
import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationResponseModel;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Selectors.by;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.refresh;
import static specs.login.LoginSpec.successfulLoginResponseSpec;

/**
 * Авторизация браузера без формы входа: токены берутся через API и кладутся в localStorage
 * в том же виде, в каком их сохраняет фронтенд после входа через форму.
 */
public class BrowserAuth {

    private static final String AUTH_STORAGE_KEY = "book_club_auth";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Step("Авторизовать браузер токеном из API (localStorage)")
    public static void loginByToken(RegistrationResponseModel user, String password) {
        SuccessfulLoginResponseModel tokens = ApiClient.login(new LoginRequestModel(user.username(), password))
                .spec(successfulLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class);

        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("user", user);
        auth.put("accessToken", tokens.access());
        auth.put("refreshToken", tokens.refresh());
        auth.put("isAuthenticated", true);

        open("/");
        executeJavaScript("localStorage.setItem(arguments[0], arguments[1])",
                AUTH_STORAGE_KEY, MAPPER.writeValueAsString(auth));
        refresh();

        $(by("data-testid", "signin-link")).should(disappear);
    }
}
