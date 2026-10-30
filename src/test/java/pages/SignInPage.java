package pages;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Selectors.by;
import static com.codeborne.selenide.Selenide.$;

public class SignInPage {

    @Step("Открыть страницу входа")
    public SignInPage open() {
        Selenide.open("/signin");
        return this;
    }

    @Step("Войти под пользователем {username}")
    public void signIn(String username, String password) {
        $(by("data-testid", "username-input")).setValue(username);
        $(by("data-testid", "password-input")).setValue(password);
        $(by("data-testid", "submit-button")).click();
        $(by("data-testid", "signin-link")).should(disappear);
    }
}
