package tests;

import client.ApiClient;
import models.clubs.ClubRequestModel;
import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.RegistrationResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.ClubPage;
import pages.ClubsListPage;
import pages.CreateClubPage;

import static auth.BrowserAuth.loginByToken;
import static data.TestData.*;
import static io.qameta.allure.Allure.step;
import static specs.clubs.ClubsSpec.successfulClubCreationResponseSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;

@Tag("ui")
public class UiClubsTests extends UiTestBase {

    private final CreateClubPage createClubPage = new CreateClubPage();
    private final ClubsListPage clubsListPage = new ClubsListPage();
    private final ClubPage clubPage = new ClubPage();

    private String username;
    private RegistrationResponseModel user;
    private String bookTitle;
    private String description;

    @BeforeEach
    public void prepareUser() {
        username = randomUsername();
        bookTitle = randomBookTitle();
        description = randomDescription();

        user = step("Зарегистрировать нового пользователя через API", () ->
                ApiClient.register(new RegistrationBodyModel(username, PASSWORD))
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(RegistrationResponseModel.class));
    }

    private void createClubViaApi() {
        step("Создать клуб от имени пользователя через API", () -> {
            String accessToken = ApiClient.login(new LoginRequestModel(username, PASSWORD))
                    .spec(successfulLoginResponseSpec)
                    .extract()
                    .as(SuccessfulLoginResponseModel.class)
                    .access();

            ApiClient.createClub(accessToken, new ClubRequestModel(
                            bookTitle, randomBookAuthors(), randomPublicationYear(),
                            description, randomTelegramChatLink()))
                    .spec(successfulClubCreationResponseSpec);
        });
    }

    @Test
    @DisplayName("Ошибка при попытке покинуть клуб владельцем")
    public void ownerCannotLeaveClubTest() {

        createClubViaApi();

        loginByToken(user, PASSWORD);

        clubsListPage.showMyClubs().openClub(bookTitle);

        clubPage.shouldShowClub(bookTitle, description)
                .leaveClubAndConfirm()
                .shouldShowError(LEAVE_CLUB_ERROR);
    }

    @Test
    @DisplayName("Поиск клуба по названию на странице списка клубов")
    public void searchClubByTitleTest() {

        createClubViaApi();

        loginByToken(user, PASSWORD);

        clubsListPage.search(bookTitle)
                .shouldShowOnlyClub(bookTitle);
    }

    @Test
    @DisplayName("Переход с главной страницы на страницу создания клуба")
    public void navigateToCreateClubPageTest() {

        loginByToken(user, PASSWORD);

        clubsListPage.openFromHeader();

        createClubPage.open()
                .shouldBeOpened();
    }

    @Test
    @DisplayName("Успешное создание клуба через форму")
    public void createClubViaFormTest() {

        loginByToken(user, PASSWORD);

        createClubPage.open()
                .fill(bookTitle, randomBookAuthors(), randomPublicationYear(), description, randomTelegramChatLink())
                .submit();

        clubsListPage.showMyClubs().openClub(bookTitle);

        clubPage.shouldShowClub(bookTitle, description);
    }
}
