package ca.gbc.approvalservice.dto;

import java.time.LocalDateTime;

public record EventResponse(String id,
                            String eventName,
                            String organizerId,
                            String eventType,
                            int expectedAttendees,
                            String roomId,
                            LocalDateTime startTime,
                            LocalDateTime endTime,
                            String Status,
                            String bookingId) {
}
