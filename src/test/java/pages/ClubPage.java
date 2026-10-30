package pages;

import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.confirm;

public class ClubPage {

    @Step("Проверить, что открыта страница клуба «{bookTitle}» с описанием")
    public ClubPage shouldShowClub(String bookTitle, String description) {
        $(".club-content .club-header h1").shouldHave(text(bookTitle));
        $(".club-content .description").shouldHave(text(description));
        return this;
    }

    @Step("Нажать «Покинуть клуб» и подтвердить выход в диалоге браузера")
    public ClubPage leaveClubAndConfirm() {
        $(".leave-btn").click();
        confirm();
        return this;
    }

    @Step("Проверить сообщение об ошибке: {message}")
    public void shouldShowError(String message) {
        $(".club-details .error").shouldBe(visible).shouldHave(text(message));
    }
}
