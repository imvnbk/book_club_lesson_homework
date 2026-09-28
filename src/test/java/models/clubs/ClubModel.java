package models.clubs;

import java.util.List;

public record ClubModel(
        Integer id,
        String bookTitle,
        String bookAuthors,
        Integer publicationYear,
        String description,
        String telegramChatLink,
        Integer owner,
        List<Integer> members,
        List<ReviewModel> reviews,
        String created,
        String modified
) {}
