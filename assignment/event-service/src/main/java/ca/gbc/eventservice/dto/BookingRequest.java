package ca.gbc.eventservice.dto;

import java.time.LocalDateTime;

public record BookingRequest(String userId,
                             String roomId,
                             LocalDateTime startTime,
                             LocalDateTime endTime,
                             String purpose
,int capacity) {
}
