package ca.gbc.userservice.dto;

public record   UserRequest(Long id,
                          String password,
                          String name,
                          String email,
                          String role,
                          String userType ) {
}
