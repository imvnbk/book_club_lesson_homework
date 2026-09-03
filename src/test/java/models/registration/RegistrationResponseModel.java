package models.registration;

public record RegistrationResponseModel(Integer id, String username, String firstName,
                                        String lastName,String email, String remoteAddr) {}
