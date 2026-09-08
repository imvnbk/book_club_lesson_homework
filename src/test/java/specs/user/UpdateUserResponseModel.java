package specs.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateUserResponseModel(
        int id,
        String username,
        String firstName,
        String lastName,
        String email)
{}
