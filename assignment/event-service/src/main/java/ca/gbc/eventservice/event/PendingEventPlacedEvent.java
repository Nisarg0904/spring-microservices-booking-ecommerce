package ca.gbc.eventservice.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingEventPlacedEvent {
    private String id;
    private String eventName;
    private String organizerId;
    private String eventType;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int expectedAttendees;
    private String status;
    private String bookingId;
    private String mail;

}
