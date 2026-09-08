package specs.user;

public record UpdateUserRequestModel(
        String firstName,
        String lastName,
        String email
) {}