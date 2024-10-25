package ca.gbc.eventservice.dto;

import java.time.LocalDateTime;

public record BookingResponse(String id,
                              String userId,
                              String roomId,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              String purpose) {
}
