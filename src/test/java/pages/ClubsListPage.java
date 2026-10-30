package pages;

import io.qameta.allure.Step;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.by;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ClubsListPage {

    @Step("Перейти на главную страницу со списком клубов")
    public ClubsListPage openFromHeader() {
        $(by("data-testid", "clubs-link")).click();
        $(".clubs-list").shouldBe(visible);
        return this;
    }

    @Step("Включить фильтр «Мои клубы»")
    public ClubsListPage showMyClubs() {
        $$(".filter-option").findBy(text("Мои клубы")).click();
        return this;
    }

    @Step("Найти клуб по названию «{bookTitle}»")
    public ClubsListPage search(String bookTitle) {
        $(".search-input").setValue(bookTitle);
        $(".search-button").click();
        return this;
    }

    @Step("Проверить, что в списке только клуб «{bookTitle}»")
    public ClubsListPage shouldShowOnlyClub(String bookTitle) {
        $$(".club-card").shouldHave(size(1));
        $$(".club-card").first().$(".club-header h2").shouldHave(text(bookTitle));
        return this;
    }

    @Step("Открыть клуб «{bookTitle}» из списка")
    public void openClub(String bookTitle) {
        $$(".club-card").findBy(text(bookTitle)).$(".open-btn").click();
    }
}
