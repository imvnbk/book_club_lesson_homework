package tests;

import client.ApiClient;
import models.clubs.ClubModel;
import models.clubs.ClubsPageResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.clubs.ClubsSpec.successfulClubsResponseSpec;

public class ClubsTests extends TestBase {

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
}
