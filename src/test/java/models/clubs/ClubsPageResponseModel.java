package models.clubs;

import java.util.List;

public record ClubsPageResponseModel(
        Integer count,
        String next,
        String previous,
        List<ClubModel> results
) {}
