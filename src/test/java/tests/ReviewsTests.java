package tests;

import client.ApiClient;
import models.clubs.ClubModel;
import models.clubs.ClubRequestModel;
import models.clubs.ReviewModel;
import models.login.LoginRequestModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.reviews.ReviewErrorResponseModel;
import models.reviews.ReviewRequestModel;
import models.reviews.ReviewsPageResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static data.TestData.*;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.clubs.ClubsSpec.successfulClubCreationResponseSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;
import static specs.reviews.ReviewsSpec.*;

/**
 * Контракт GET /clubs/reviews/ подтверждён примером ответа. Контракт POST/PUT/PATCH/DELETE
 * и коды ошибок (400 / 401 / 403 / 404) выведены из ReviewModel и логики CRUD клубов —
 * при расхождении с реальным API поправить ожидания в ReviewsSpec.
 * <p>
 * Каждый тест работает в собственном клубе и с новыми пользователями, чтобы не упираться
 * в ограничения на повторные отзывы и не зависеть от общих данных.
 */
public class ReviewsTests extends TestBase {

    private static final int DEMO_CLUB_ID = 1;

    private String review;
    private Integer assessment;
    private Integer readPages;

    @BeforeEach
    public void prepareReviewData() {
        review = randomReview();
        assessment = randomAssessment();
        readPages = randomReadPages();
    }

    private String login(String username, String password) {
        return ApiClient.login(new LoginRequestModel(username, password))
                .spec(successfulLoginResponseSpec)
                .extract()
                .as(SuccessfulLoginResponseModel.class)
                .access();
    }

    private String registerUserAndGetToken() {
        return step("Зарегистрировать нового пользователя и получить access токен", () -> {
            String username = randomUsername();
            ApiClient.register(new RegistrationBodyModel(username, PASSWORD))
                    .spec(successfulRegistrationResponseSpec);
            return login(username, PASSWORD);
        });
    }

    private int createClubId() {
        return step("Создать клуб для отзывов", () -> {
            String ownerToken = login(USERNAME, PASSWORD);
            ClubRequestModel club = new ClubRequestModel(
                    randomBookTitle(), randomBookAuthors(), randomPublicationYear(),
                    randomDescription(), randomTelegramChatLink());
            return ApiClient.createClub(ownerToken, club)
                    .spec(successfulClubCreationResponseSpec)
                    .extract()
                    .as(ClubModel.class)
                    .id();
        });
    }

    private ReviewModel createReview(String accessToken, int clubId) {
        return step("Создать отзыв в клубе " + clubId, () ->
                ApiClient.createReview(accessToken, new ReviewRequestModel(clubId, review, assessment, readPages))
                        .spec(successfulReviewCreationResponseSpec)
                        .extract()
                        .as(ReviewModel.class));
    }

    // ---------- Чтение ----------

    @Test
    @DisplayName("Получение списка отзывов клуба без авторизации")
    public void successfulGetReviewsTest() {

        ReviewsPageResponseModel response = step("Запросить отзывы клуба " + DEMO_CLUB_ID + " без токена", () ->
                ApiClient.getReviews(DEMO_CLUB_ID)
                        .spec(successfulReviewsResponseSpec)
                        .extract()
                        .as(ReviewsPageResponseModel.class));

        step("Проверить, что вернулись только отзывы запрошенного клуба", () -> {
            assertThat(response.count()).isPositive();
            assertThat(response.results()).isNotEmpty();
            assertThat(response.results()).allSatisfy(r -> {
                assertThat(r.club()).isEqualTo(DEMO_CLUB_ID);
                assertThat(r.user().username()).isNotBlank();
                assertThat(r.assessment()).isBetween(1, 5);
            });
        });
    }

    @Test
    @DisplayName("Пагинация списка отзывов через page и page_size")
    public void reviewsPaginationTest() {

        ReviewsPageResponseModel response = step("Запросить первую страницу из одного отзыва", () ->
                ApiClient.getReviews(DEMO_CLUB_ID, 1, 1)
                        .spec(successfulReviewsResponseSpec)
                        .extract()
                        .as(ReviewsPageResponseModel.class));

        step("Проверить размер страницы и ссылки пагинации", () -> {
            assertThat(response.results()).hasSize(1);
            assertThat(response.count()).isGreaterThan(1);
            assertThat(response.previous()).isNull();
            assertThat(response.next()).contains("page=2");
        });
    }

    @Test
    @DisplayName("Успешное получение отзыва по id")
    public void successfulGetReviewByIdTest() {

        int clubId = createClubId();
        ReviewModel created = createReview(registerUserAndGetToken(), clubId);

        ReviewModel response = step("Получить отзыв по id", () ->
                ApiClient.getReview(created.id())
                        .spec(successfulReviewResponseSpec)
                        .extract()
                        .as(ReviewModel.class));

        step("Проверить, что полученный отзыв соответствует созданному", () ->
                assertThat(response).isEqualTo(created));
    }

    @Test
    @DisplayName("Ошибка 404 при запросе несуществующего отзыва")
    public void getNonExistingReviewTest() {

        ReviewErrorResponseModel response = step("Запросить отзыв с несуществующим id", () ->
                ApiClient.getReview(NON_EXISTING_REVIEW_ID)
                        .spec(reviewNotFoundResponseSpec)
                        .extract()
                        .as(ReviewErrorResponseModel.class));

        step("Проверить, что в ответе есть сообщение об ошибке", () ->
                assertThat(response.detail()).isNotBlank());
    }

    // ---------- CRUD ----------

    @Test
    @DisplayName("Успешное создание отзыва авторизованным пользователем")
    public void successfulCreateReviewTest() {

        int clubId = createClubId();
        String accessToken = registerUserAndGetToken();

        ReviewModel response = createReview(accessToken, clubId);

        step("Проверить, что данные созданного отзыва соответствуют отправленным", () -> {
            assertThat(response.id()).isNotNull();
            assertThat(response.club()).isEqualTo(clubId);
            assertThat(response.review()).isEqualTo(review);
            assertThat(response.assessment()).isEqualTo(assessment);
            assertThat(response.readPages()).isEqualTo(readPages);
            assertThat(response.user().username()).isNotBlank();
            assertThat(response.modified()).isNull();
        });

        step("Проверить, что отзыв появился в списке отзывов клуба", () -> {
            ReviewsPageResponseModel list = ApiClient.getReviews(clubId)
                    .spec(successfulReviewsResponseSpec)
                    .extract()
                    .as(ReviewsPageResponseModel.class);
            assertThat(list.results()).extracting(ReviewModel::id).contains(response.id());
        });
    }

    @Test
    @DisplayName("Успешное полное обновление отзыва автором (PUT)")
    public void successfulUpdateReviewTest() {

        int clubId = createClubId();
        String accessToken = registerUserAndGetToken();
        ReviewModel created = createReview(accessToken, clubId);

        ReviewRequestModel updatedData = new ReviewRequestModel(
                clubId, randomReview(), randomAssessment(), randomReadPages());

        ReviewModel response = step("Обновить отзыв новыми данными", () ->
                ApiClient.updateReview(accessToken, created.id(), updatedData)
                        .spec(successfulReviewResponseSpec)
                        .extract()
                        .as(ReviewModel.class));

        step("Проверить, что данные отзыва обновились", () -> {
            assertThat(response.id()).isEqualTo(created.id());
            assertThat(response.review()).isEqualTo(updatedData.review());
            assertThat(response.assessment()).isEqualTo(updatedData.assessment());
            assertThat(response.readPages()).isEqualTo(updatedData.readPages());
            assertThat(response.modified()).isNotNull();
        });
    }

    @Test
    @DisplayName("Успешное частичное обновление отзыва автором (PATCH)")
    public void successfulPatchReviewTest() {

        int clubId = createClubId();
        String accessToken = registerUserAndGetToken();
        ReviewModel created = createReview(accessToken, clubId);

        String newText = randomReview();

        ReviewModel response = step("Изменить только текст отзыва", () ->
                ApiClient.patchReview(accessToken, created.id(), Map.of("review", newText))
                        .spec(successfulReviewResponseSpec)
                        .extract()
                        .as(ReviewModel.class));

        step("Проверить, что изменился только текст отзыва", () -> {
            assertThat(response.id()).isEqualTo(created.id());
            assertThat(response.review()).isEqualTo(newText);
            assertThat(response.assessment()).isEqualTo(created.assessment());
            assertThat(response.readPages()).isEqualTo(created.readPages());
        });
    }

    @Test
    @DisplayName("Успешное удаление отзыва автором")
    public void successfulDeleteReviewTest() {

        int clubId = createClubId();
        String accessToken = registerUserAndGetToken();
        ReviewModel created = createReview(accessToken, clubId);

        step("Удалить отзыв", () ->
                ApiClient.deleteReview(accessToken, created.id())
                        .spec(reviewDeletedResponseSpec));

        step("Проверить, что удалённый отзыв больше не доступен", () ->
                ApiClient.getReview(created.id())
                        .spec(reviewNotFoundResponseSpec));
    }

    // ---------- Валидация ----------

    @ParameterizedTest(name = "Ошибка 400 при создании отзыва с оценкой {0}")
    @ValueSource(ints = {0, 6, -1})
    @DisplayName("Ошибка 400 при создании отзыва с оценкой вне диапазона 1-5")
    public void createReviewWithInvalidAssessmentTest(int invalidAssessment) {

        int clubId = createClubId();
        String accessToken = registerUserAndGetToken();

        step("Отправить отзыв с недопустимой оценкой " + invalidAssessment, () ->
                ApiClient.createReview(accessToken, new ReviewRequestModel(clubId, review, invalidAssessment, readPages))
                        .spec(reviewValidationErrorResponseSpec));
    }

    // ---------- Права доступа ----------

    @Test
    @DisplayName("Ошибка 401 при создании отзыва без авторизации")
    public void createReviewWithoutAuthTest() {

        ReviewRequestModel data = new ReviewRequestModel(DEMO_CLUB_ID, review, assessment, readPages);

        ReviewErrorResponseModel response = step("Отправить запрос на создание отзыва без токена", () ->
                ApiClient.createReview(data)
                        .spec(reviewUnauthorizedResponseSpec)
                        .extract()
                        .as(ReviewErrorResponseModel.class));

        step("Проверить сообщение об ошибке отсутствия авторизации", () ->
                assertThat(response.detail()).isEqualTo(AUTH_CREDENTIALS_NOT_PROVIDED_ERROR));
    }

    @Test
    @DisplayName("Ошибка 401 при обновлении (PUT) и частичном обновлении (PATCH) отзыва без авторизации")
    public void updateReviewWithoutAuthTest() {

        int clubId = createClubId();
        ReviewModel created = createReview(registerUserAndGetToken(), clubId);

        step("Отправить PUT без токена", () ->
                ApiClient.updateReview(created.id(), new ReviewRequestModel(clubId, randomReview(), assessment, readPages))
                        .spec(reviewUnauthorizedResponseSpec));

        step("Отправить PATCH без токена", () ->
                ApiClient.patchReview(created.id(), Map.of("review", randomReview()))
                        .spec(reviewUnauthorizedResponseSpec));

        step("Проверить, что отзыв не изменился", () ->
                assertThat(getReviewById(created.id())).isEqualTo(created));
    }

    @Test
    @DisplayName("Ошибка 401 при удалении отзыва без авторизации")
    public void deleteReviewWithoutAuthTest() {

        int clubId = createClubId();
        ReviewModel created = createReview(registerUserAndGetToken(), clubId);

        step("Отправить DELETE без токена", () ->
                ApiClient.deleteReview(created.id())
                        .spec(reviewUnauthorizedResponseSpec));

        step("Проверить, что отзыв по-прежнему существует", () ->
                assertThat(getReviewById(created.id())).isEqualTo(created));
    }

    @Test
    @DisplayName("Ошибка 403 при обновлении (PUT) чужого отзыва")
    public void updateForeignReviewTest() {

        int clubId = createClubId();
        ReviewModel created = createReview(registerUserAndGetToken(), clubId);
        String strangerToken = registerUserAndGetToken();

        ReviewErrorResponseModel response = step("Обновить чужой отзыв другим пользователем", () ->
                ApiClient.updateReview(strangerToken, created.id(),
                                new ReviewRequestModel(clubId, randomReview(), randomAssessment(), randomReadPages()))
                        .spec(reviewForbiddenResponseSpec)
                        .extract()
                        .as(ReviewErrorResponseModel.class));

        step("Проверить сообщение об ошибке и что отзыв не изменился", () -> {
            assertThat(response.detail()).isNotBlank();
            assertThat(getReviewById(created.id())).isEqualTo(created);
        });
    }

    @Test
    @DisplayName("Ошибка 403 при частичном обновлении (PATCH) чужого отзыва")
    public void patchForeignReviewTest() {

        int clubId = createClubId();
        ReviewModel created = createReview(registerUserAndGetToken(), clubId);
        String strangerToken = registerUserAndGetToken();

        step("Изменить текст чужого отзыва другим пользователем", () ->
                ApiClient.patchReview(strangerToken, created.id(), Map.of("review", randomReview()))
                        .spec(reviewForbiddenResponseSpec));

        step("Проверить, что отзыв не изменился", () ->
                assertThat(getReviewById(created.id())).isEqualTo(created));
    }

    @Test
    @DisplayName("Ошибка 403 при удалении чужого отзыва")
    public void deleteForeignReviewTest() {

        int clubId = createClubId();
        ReviewModel created = createReview(registerUserAndGetToken(), clubId);
        String strangerToken = registerUserAndGetToken();

        step("Удалить чужой отзыв другим пользователем", () ->
                ApiClient.deleteReview(strangerToken, created.id())
                        .spec(reviewForbiddenResponseSpec));

        step("Проверить, что отзыв по-прежнему существует", () ->
                assertThat(getReviewById(created.id())).isEqualTo(created));
    }

    private ReviewModel getReviewById(int id) {
        return step("Получить отзыв по id " + id, () ->
                ApiClient.getReview(id)
                        .spec(successfulReviewResponseSpec)
                        .extract()
                        .as(ReviewModel.class));
    }
}
