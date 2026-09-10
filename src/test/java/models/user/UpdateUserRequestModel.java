package models.user;

public record UpdateUserRequestModel(
        String firstName,
        String lastName,
        String email
) {}