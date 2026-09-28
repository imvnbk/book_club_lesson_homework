package models.clubs;

public record ReviewModel(
        Integer id,
        Integer club,
        ReviewUserModel user,
        String review,
        Integer assessment,
        Integer readPages,
        String created,
        String modified
) {}
