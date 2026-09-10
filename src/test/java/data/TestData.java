package data;

import net.datafaker.Faker;

public class TestData {

    private static final Faker faker = new Faker();

    // Fixed credentials
    public static final String USERNAME = "qaguru";
    public static final String PASSWORD = "qaguru123";
    public static final String WRONG_PASSWORD = "wrongPassword";

    // Registration inputs
    public static final String INVALID_USERNAME = "invalid username";

    // Expected values
    public static final String JWT_PREFIX = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    public static final String IP_ADDRESS_REGEX = "\\d{1,3}(\\.\\d{1,3}){3}";
    public static final String EMPTY_JSON_BODY = "{}";
    public static final String REGISTER_LOCATION_PATH = "/api/v1/users/register/";

    // Expected error messages
    public static final String INVALID_CREDENTIALS_ERROR = "Invalid username or password.";
    public static final String TOKEN_BLACKLISTED_ERROR = "Token is blacklisted";
    public static final String TOKEN_NOT_VALID_CODE = "token_not_valid";
    public static final String BLANK_FIELD_ERROR = "This field may not be blank.";
    public static final String INVALID_USERNAME_ERROR =
            "Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters.";
    public static final String UNSUPPORTED_MEDIA_TYPE_ERROR =
            "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";
    public static final String EXISTING_USER_ERROR = "A user with that username already exists.";

    // Random generators
    public static String randomFirstName() {
        return faker.name().firstName();
    }

    public static String randomLastName() {
        return faker.name().lastName();
    }

    public static String randomEmail() {
        return faker.internet().emailAddress();
    }
}
