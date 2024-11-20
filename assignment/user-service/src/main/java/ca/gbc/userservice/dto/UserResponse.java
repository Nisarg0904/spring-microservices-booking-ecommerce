package ca.gbc.userservice.dto;

public record UserResponse(  Long id,
         String name,
         String email,
         String role,
         String userType ) {
}
