package ca.gbc.eventservice.dto;

public record UserResponse(String id,
                           String name,
                           String email,
                           String role,
                           String userType ) {
}
