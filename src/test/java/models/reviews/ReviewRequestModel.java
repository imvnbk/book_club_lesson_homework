package models.reviews;

public record ReviewRequestModel(
        Integer club,
        String review,
        Integer assessment,
        Integer readPages
) {}
