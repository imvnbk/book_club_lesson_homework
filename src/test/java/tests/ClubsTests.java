package tests;

import client.ApiClient;
import models.clubs.ClubModel;
import models.clubs.ClubRequestModel;
import models.clubs.ClubUnauthorizedResponseModel;
import models.clubs.ClubsPageResponseModel;
import models.clubs.ExistingBookTitleResponseModel;
import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static data.TestData.*;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.clubs.ClubsSpec.*;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;

public class ClubsTests extends TestBase {

    private String bookTitle;
    private String bookAuthors;
    private Integer publicationYear;
    private String description;
    private String telegramChatLink;

    @BeforeEach
    public void prepareClubData() {
        bookTitle = randomBookTitle();
        bookAuthors = randomBookAuthors();
        publicationYear = randomPublicationYear();
        description = randomDescription();
        telegramChatLink = randomTelegramChatLink();
    }

    private String getAccessToken() {
        return step("Авторизоваться и получить access токен", () ->
                ApiClient.login(new LoginRequestModel(USERNAME, PASSWORD))
                        .spec(successfulLoginResponseSpec)
                        .extract()
                        .as(SuccessfulLoginResponseModel.class)
                        .access());
    }

    @Test
    @DisplayName("Успешное получение первой страницы списка книжных клубов")
    public void successfulGetClubsTest() {

        ClubsPageResponseModel response = step("Отправить запрос на получение списка книжных клубов", () ->
                ApiClient.getClubs()
                        .spec(successfulClubsResponseSpec)
                        .extract()
                        .as(ClubsPageResponseModel.class));

        step("Проверить пагинацию первой страницы", () -> {
            assertThat(response.count()).isPositive();
            assertThat(response.results()).isNotEmpty();
            assertThat(response.previous()).isNull();
            assertThat(response.next()).contains("page=2");
        });

        step("Проверить структуру первого клуба в списке", () -> {
            ClubModel firstClub = response.results().get(0);

            assertThat(firstClub.id()).isNotNull();
            assertThat(firstClub.bookTitle()).isNotBlank();
            assertThat(firstClub.bookAuthors()).isNotBlank();
            assertThat(firstClub.owner()).isNotNull();
            assertThat(firstClub.members()).isNotEmpty();
            assertThat(firstClub.created()).isNotBlank();
        });
    }

    @Test
    @DisplayName("Переход на вторую страницу списка клубов через параметр page")
    public void secondPageOfClubsTest() {

        ClubsPageResponseModel response = step("Отправить запрос на получение второй страницы списка клубов", () ->
                ApiClient.getClubs(2)
                        .spec(successfulClubsResponseSpec)
                        .extract()
                        .as(ClubsPageResponseModel.class));

        step("Проверить, что вторая страница содержит клубы и ссылку на предыдущую страницу", () -> {
            assertThat(response.results()).isNotEmpty();
            assertThat(response.previous()).isNotNull();
        });
    }

    @Test
    @DisplayName("Успешное создание книжного клуба авторизованным пользователем")
    public void successfulCreateClubTest() {

        String accessToken = getAccessToken();

        ClubRequestModel data = new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);

        ClubModel response = step("Создать книжный клуб с валидными данными", () ->
                ApiClient.createClub(accessToken, data)
                        .spec(successfulClubCreationResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        step("Проверить, что данные созданного клуба соответствуют отправленным", () -> {
            assertThat(response.id()).isNotNull();
            assertThat(response.bookTitle()).isEqualTo(bookTitle);
            assertThat(response.bookAuthors()).isEqualTo(bookAuthors);
            assertThat(response.publicationYear()).isEqualTo(publicationYear);
            assertThat(response.description()).isEqualTo(description);
            assertThat(response.telegramChatLink()).isEqualTo(telegramChatLink);
            assertThat(response.owner()).isNotNull();
        });
    }

    @Test
    @DisplayName("Ошибка 401 при создании клуба без авторизации")
    public void createClubWithoutAuthTest() {

        ClubRequestModel data = new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);

        ClubUnauthorizedResponseModel response = step("Отправить запрос на создание клуба без токена авторизации", () ->
                ApiClient.createClub(data)
                        .spec(clubUnauthorizedResponseSpec)
                        .extract()
                        .as(ClubUnauthorizedResponseModel.class));

        step("Проверить сообщение об ошибке отсутствия авторизации", () ->
                assertThat(response.detail()).isEqualTo(AUTH_CREDENTIALS_NOT_PROVIDED_ERROR));
    }

    @Test
    @DisplayName("Ошибка 400 при создании клуба с уже существующим названием книги")
    public void createClubWithExistingBookTitleTest() {

        String accessToken = getAccessToken();

        ClubRequestModel data = new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);

        step("Создать книжный клуб первый раз", () ->
                ApiClient.createClub(accessToken, data)
                        .spec(successfulClubCreationResponseSpec));

        ExistingBookTitleResponseModel response = step("Повторно создать клуб с тем же названием книги", () ->
                ApiClient.createClub(accessToken, data)
                        .spec(existingBookTitleClubResponseSpec)
                        .extract()
                        .as(ExistingBookTitleResponseModel.class));

        step("Проверить сообщение об ошибке дублирующегося названия книги", () ->
                assertThat(response.bookTitle()).containsExactly(EXISTING_BOOK_TITLE_ERROR));
    }

    @Test
    @DisplayName("Успешное получение клуба по id")
    public void successfulGetClubByIdTest() {

        String accessToken = getAccessToken();

        ClubRequestModel data = new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);

        ClubModel createdClub = step("Создать книжный клуб для последующего получения", () ->
                ApiClient.createClub(accessToken, data)
                        .spec(successfulClubCreationResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        ClubModel response = step("Получить клуб по id", () ->
                ApiClient.getClub(createdClub.id())
                        .spec(successfulClubResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        step("Проверить, что полученный клуб соответствует созданному", () -> {
            assertThat(response.id()).isEqualTo(createdClub.id());
            assertThat(response.bookTitle()).isEqualTo(bookTitle);
            assertThat(response.bookAuthors()).isEqualTo(bookAuthors);
        });
    }

    @Test
    @DisplayName("Успешное полное обновление клуба (PUT)")
    public void successfulUpdateClubTest() {

        String accessToken = getAccessToken();

        ClubRequestModel initialData = new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);

        ClubModel createdClub = step("Создать книжный клуб для последующего обновления", () ->
                ApiClient.createClub(accessToken, initialData)
                        .spec(successfulClubCreationResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        ClubRequestModel updatedData = new ClubRequestModel(
                randomBookTitle(), randomBookAuthors(), randomPublicationYear(), randomDescription(), randomTelegramChatLink());

        ClubModel response = step("Обновить клуб новыми данными", () ->
                ApiClient.updateClub(accessToken, createdClub.id(), updatedData)
                        .spec(successfulClubResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        step("Проверить, что данные клуба обновились", () -> {
            assertThat(response.id()).isEqualTo(createdClub.id());
            assertThat(response.bookTitle()).isEqualTo(updatedData.bookTitle());
            assertThat(response.bookAuthors()).isEqualTo(updatedData.bookAuthors());
            assertThat(response.publicationYear()).isEqualTo(updatedData.publicationYear());
            assertThat(response.description()).isEqualTo(updatedData.description());
            assertThat(response.telegramChatLink()).isEqualTo(updatedData.telegramChatLink());
        });
    }

    @Test
    @DisplayName("Успешное обновление клуба через PATCH")
    public void successfulPatchClubTest() {

        String accessToken = getAccessToken();

        ClubRequestModel initialData = new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);

        ClubModel createdClub = step("Создать книжный клуб для последующего обновления", () ->
                ApiClient.createClub(accessToken, initialData)
                        .spec(successfulClubCreationResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        ClubRequestModel patchData = new ClubRequestModel(
                randomBookTitle(), randomBookAuthors(), randomPublicationYear(), randomDescription(), randomTelegramChatLink());

        ClubModel response = step("Частично обновить клуб (PATCH)", () ->
                ApiClient.patchClub(accessToken, createdClub.id(), patchData)
                        .spec(successfulClubResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        step("Проверить, что данные клуба обновились", () -> {
            assertThat(response.id()).isEqualTo(createdClub.id());
            assertThat(response.bookTitle()).isEqualTo(patchData.bookTitle());
            assertThat(response.bookAuthors()).isEqualTo(patchData.bookAuthors());
        });
    }

    @Test
    @DisplayName("Успешное удаление клуба")
    public void successfulDeleteClubTest() {

        String accessToken = getAccessToken();

        ClubRequestModel data = new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);

        ClubModel createdClub = step("Создать книжный клуб для последующего удаления", () ->
                ApiClient.createClub(accessToken, data)
                        .spec(successfulClubCreationResponseSpec)
                        .extract()
                        .as(ClubModel.class));

        step("Удалить созданный клуб", () ->
                ApiClient.deleteClub(accessToken, createdClub.id())
                        .spec(clubDeletedResponseSpec));
    }

    // ---------- Негативные сценарии и права ----------
    // Допущение: коды 400 / 401 / 403 / 404 выведены по аналогии с отзывами и логикой DRF.

    private ClubRequestModel validClubData() {
        return new ClubRequestModel(bookTitle, bookAuthors, publicationYear, description, telegramChatLink);
    }

    private ClubRequestModel randomClubData() {
        return new ClubRequestModel(
                randomBookTitle(), randomBookAuthors(), randomPublicationYear(), randomDescription(), randomTelegramChatLink());
    }

    private ClubModel createClubAs(String accessToken, ClubRequestModel data) {
        return step("Создать книжный клуб", () ->
                ApiClient.createClub(accessToken, data)
                        .spec(successfulClubCreationResponseSpec)
                        .extract()
                        .as(ClubModel.class));
    }

    private ClubModel getClubById(int id) {
        return step("Получить клуб по id " + id, () ->
                ApiClient.getClub(id)
                        .spec(successfulClubResponseSpec)
                        .extract()
                        .as(ClubModel.class));
    }

    private String registerStrangerAndGetToken() {
        return step("Зарегистрировать постороннего пользователя и получить access токен", () -> {
            String username = randomUsername();
            ApiClient.register(new RegistrationBodyModel(username, PASSWORD))
                    .spec(successfulRegistrationResponseSpec);
            return ApiClient.login(new LoginRequestModel(username, PASSWORD))
                    .spec(successfulLoginResponseSpec)
                    .extract()
                    .as(SuccessfulLoginResponseModel.class)
                    .access();
        });
    }

    @Test
    @DisplayName("Ошибка 400 при создании клуба с пустым названием книги")
    public void createClubWithBlankBookTitleTest() {

        String accessToken = getAccessToken();

        step("Отправить запрос на создание клуба с пустым названием", () ->
                ApiClient.createClub(accessToken,
                                new ClubRequestModel("", bookAuthors, publicationYear, description, telegramChatLink))
                        .spec(clubValidationErrorResponseSpec));
    }

    @Test
    @DisplayName("Ошибка 400 при создании клуба без обязательных полей")
    public void createClubWithoutRequiredFieldsTest() {

        String accessToken = getAccessToken();

        step("Отправить запрос на создание клуба без данных", () ->
                ApiClient.createClub(accessToken, new ClubRequestModel(null, null, null, null, null))
                        .spec(clubValidationErrorResponseSpec));
    }

    @Test
    @DisplayName("Ошибка 400 при обновлении клуба названием книги другого клуба")
    public void updateClubWithExistingBookTitleTest() {

        String accessToken = getAccessToken();

        ClubModel first = createClubAs(accessToken, validClubData());
        ClubModel second = createClubAs(accessToken, randomClubData());

        ExistingBookTitleResponseModel response = step("Обновить второй клуб названием книги первого", () ->
                ApiClient.updateClub(accessToken, second.id(), new ClubRequestModel(
                                first.bookTitle(), second.bookAuthors(), second.publicationYear(),
                                second.description(), second.telegramChatLink()))
                        .spec(existingBookTitleClubResponseSpec)
                        .extract()
                        .as(ExistingBookTitleResponseModel.class));

        step("Проверить сообщение об ошибке дублирующегося названия книги", () ->
                assertThat(response.bookTitle()).containsExactly(EXISTING_BOOK_TITLE_ERROR));
    }

    @Test
    @DisplayName("Ошибка 404 при запросе несуществующего клуба")
    public void getNonExistingClubTest() {

        ClubUnauthorizedResponseModel response = step("Запросить клуб с несуществующим id", () ->
                ApiClient.getClub(NON_EXISTING_CLUB_ID)
                        .spec(clubNotFoundResponseSpec)
                        .extract()
                        .as(ClubUnauthorizedResponseModel.class));

        step("Проверить, что в ответе есть сообщение об ошибке", () ->
                assertThat(response.detail()).isNotBlank());
    }

    @Test
    @DisplayName("Удалённый клуб недоступен по id")
    public void getDeletedClubTest() {

        String accessToken = getAccessToken();
        ClubModel created = createClubAs(accessToken, validClubData());

        step("Удалить клуб", () ->
                ApiClient.deleteClub(accessToken, created.id())
                        .spec(clubDeletedResponseSpec));

        step("Проверить, что клуб больше не доступен", () ->
                ApiClient.getClub(created.id())
                        .spec(clubNotFoundResponseSpec));
    }

    @Test
    @DisplayName("Ошибка 404 при запросе страницы списка клубов за пределами диапазона")
    public void getClubsPageOutOfRangeTest() {

        step("Запросить несуществующую страницу списка клубов", () ->
                ApiClient.getClubs(NON_EXISTING_PAGE)
                        .spec(clubNotFoundResponseSpec));
    }

    @Test
    @DisplayName("Ошибка 401 при обновлении (PUT) и частичном обновлении (PATCH) клуба без авторизации")
    public void updateClubWithoutAuthTest() {

        ClubModel created = createClubAs(getAccessToken(), validClubData());

        step("Отправить PUT без токена", () ->
                ApiClient.updateClub(created.id(), randomClubData())
                        .spec(clubUnauthorizedResponseSpec));

        step("Отправить PATCH без токена", () ->
                ApiClient.patchClub(created.id(), randomClubData())
                        .spec(clubUnauthorizedResponseSpec));

        step("Проверить, что клуб не изменился", () ->
                assertThat(getClubById(created.id()).bookTitle()).isEqualTo(created.bookTitle()));
    }

    @Test
    @DisplayName("Ошибка 401 при удалении клуба без авторизации")
    public void deleteClubWithoutAuthTest() {

        ClubModel created = createClubAs(getAccessToken(), validClubData());

        step("Отправить DELETE без токена", () ->
                ApiClient.deleteClub(created.id())
                        .spec(clubUnauthorizedResponseSpec));

        step("Проверить, что клуб по-прежнему существует", () ->
                assertThat(getClubById(created.id()).id()).isEqualTo(created.id()));
    }

    @Test
    @DisplayName("Ошибка 403 при обновлении (PUT) чужого клуба")
    public void updateForeignClubTest() {

        ClubModel created = createClubAs(getAccessToken(), validClubData());
        String strangerToken = registerStrangerAndGetToken();

        step("Обновить чужой клуб другим пользователем", () ->
                ApiClient.updateClub(strangerToken, created.id(), randomClubData())
                        .spec(clubForbiddenResponseSpec));

        step("Проверить, что клуб не изменился", () ->
                assertThat(getClubById(created.id()).bookTitle()).isEqualTo(created.bookTitle()));
    }

    @Test
    @DisplayName("Ошибка 403 при частичном обновлении (PATCH) чужого клуба")
    public void patchForeignClubTest() {

        ClubModel created = createClubAs(getAccessToken(), validClubData());
        String strangerToken = registerStrangerAndGetToken();

        step("Изменить чужой клуб другим пользователем", () ->
                ApiClient.patchClub(strangerToken, created.id(), randomClubData())
                        .spec(clubForbiddenResponseSpec));

        step("Проверить, что клуб не изменился", () ->
                assertThat(getClubById(created.id()).bookTitle()).isEqualTo(created.bookTitle()));
    }

    @Test
    @DisplayName("Ошибка 403 при удалении чужого клуба")
    public void deleteForeignClubTest() {

        ClubModel created = createClubAs(getAccessToken(), validClubData());
        String strangerToken = registerStrangerAndGetToken();

        step("Удалить чужой клуб другим пользователем", () ->
                ApiClient.deleteClub(strangerToken, created.id())
                        .spec(clubForbiddenResponseSpec));

        step("Проверить, что клуб по-прежнему существует", () ->
                assertThat(getClubById(created.id()).id()).isEqualTo(created.id()));
    }
}
