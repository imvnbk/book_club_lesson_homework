package models.reviews;

import models.clubs.ReviewModel;

import java.util.List;

public record ReviewsPageResponseModel(
        Integer count,
        String next,
        String previous,
        List<ReviewModel> results
) {}
