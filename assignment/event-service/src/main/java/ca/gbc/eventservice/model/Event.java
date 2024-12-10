package ca.gbc.eventservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
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
}
