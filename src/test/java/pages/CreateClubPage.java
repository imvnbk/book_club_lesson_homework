package pages;

import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.by;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static com.codeborne.selenide.Selenide.webdriver;

public class CreateClubPage {

    @Step("Перейти к форме создания клуба")
    public CreateClubPage open() {
        $(by("data-testid", "create-club-link")).click();
        return this;
    }

    @Step("Проверить, что открыта страница создания клуба")
    public CreateClubPage shouldBeOpened() {
        webdriver().shouldHave(urlContaining("/clubs/create"));
        $("form button.submit-btn").shouldBe(visible);
        return this;
    }

    @Step("Заполнить форму создания клуба")
    public CreateClubPage fill(String bookTitle, String bookAuthors, int publicationYear,
                               String description, String telegramChatLink) {
        $("#bookTitle").setValue(bookTitle);
        $("#bookAuthors").setValue(bookAuthors);
        $("#publicationYear").setValue(String.valueOf(publicationYear));
        $("#description").setValue(description);
        $("#telegramChatLink").setValue(telegramChatLink);
        return this;
    }

    @Step("Отправить форму создания клуба")
    public void submit() {
        $("form button.submit-btn").click();
    }
}
