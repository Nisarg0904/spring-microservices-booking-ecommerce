package ca.gbc.eventservice.consumer;

import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.event.BookingPlacedEvent;
import ca.gbc.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final EventService eventService;

    @KafkaListener(topics = "booking-created", groupId = "event-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBookingCreatedEvent(BookingPlacedEvent bookingPlacedEvent) {
        log.info("Received BookingPlacedEvent: {}", bookingPlacedEvent);

        try {
            // Map BookingPlacedEvent to EventRequest
            EventRequest eventRequest = mapToEventRequest(bookingPlacedEvent);

            // Call EventService to create the event
            EventResponse eventResponse = eventService.createEvent(eventRequest);

            log.info("Event created successfully: {}", eventResponse);
        } catch (Exception e) {
            log.error("Failed to process BookingPlacedEvent: {}", e.getMessage());
        }
    }

    private EventRequest mapToEventRequest(BookingPlacedEvent bookingPlacedEvent) {
        return new EventRequest(
                null,                                // id (will be auto-generated)
                "Auto-Generated Event",             // eventName (example value)
                bookingPlacedEvent.getEmail(),      // organizerId
                "General",                          // eventType (example value)
                10,                                 // expectedAttendees (example value)
                bookingPlacedEvent.getRoomId(),     // roomId
                bookingPlacedEvent.getStartTime(),  // startTime
                bookingPlacedEvent.getEndTime(),    // endTime
                "PENDING",                          // status
                null                                // bookingId (will be set later)
        );
    }
}

