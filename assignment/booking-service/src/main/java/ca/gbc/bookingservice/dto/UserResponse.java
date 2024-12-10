package ca.gbc.bookingservice.dto;

public record UserResponse(Long id,
                           String name,
                           String email,
                           String role,
                           String userType ) {
}
