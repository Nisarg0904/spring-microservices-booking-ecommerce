package ca.gbc.eventservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingPlacedEvent {
    private String email;
    private String bookingPurpose;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
