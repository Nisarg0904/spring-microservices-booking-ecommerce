package ca.gbc.bookingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "bookings")

public class Booking {
    @Id
    private String id;
    private String userId;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
}
