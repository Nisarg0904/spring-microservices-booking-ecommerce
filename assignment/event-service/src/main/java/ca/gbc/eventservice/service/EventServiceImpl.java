package ca.gbc.eventservice.service;

import ca.gbc.eventservice.client.BookingClient;
import ca.gbc.eventservice.client.RoomClient;
import ca.gbc.eventservice.client.UserClient;
import ca.gbc.eventservice.dto.BookingRequest;
import ca.gbc.eventservice.dto.BookingResponse;
import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.model.Event;
import ca.gbc.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final BookingClient bookingClient;
    private final UserClient userClient;
    private final RoomClient roomClient;
    private final EventRepository eventRepository;

    private EventResponse mapToEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getOrganizerId(),
                event.getEventType(),
                event.getExpectedAttendees(),
                event.getRoomId(),
                event.getStartTime(),
                event.getEndTime(),
                event.getStatus(),
                event.getBookingId()
        );
    }

    @Override
    public EventResponse createEvent(EventRequest eventRequest) {
        // Fetch organizer type
        String organizerType = userClient.getUserType(eventRequest.organizerId());

        // Check constraints for students
        if ("student".equalsIgnoreCase(organizerType) && eventRequest.expectedAttendees() > 25) {
            log.error("Event creation failed: Students cannot organize events with more than 25 attendees.");
            throw new IllegalArgumentException("Students cannot organize events with more than 25 attendees.");
        }

        // Check room capacity
        if(!Objects.equals(eventRequest.roomId(), "")) {
            if (eventRequest.expectedAttendees() > roomClient.getRoomCapacity(eventRequest.roomId())) {
                log.error("Event creation failed: Room capacity exceeded.");
                throw new IllegalArgumentException("Room capacity exceeded.");
            }
        }

        // Make a booking for the event
        String bookingId;
        String roomId;
        try {
            BookingRequest bookingRequest = new BookingRequest(
                    eventRequest.organizerId(),
                    eventRequest.roomId(),
                    eventRequest.startTime(),
                    eventRequest.endTime(),
                    "Event Booking"
                    ,eventRequest.expectedAttendees()
            );
            BookingResponse bookingResponse = bookingClient.makeBooking(bookingRequest);
            bookingId = bookingResponse.id();
            roomId = bookingResponse.roomId();

        } catch (Exception e) {
            log.error("Booking creation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Booking failed: " + e.getMessage());
        }

        // Save the event
        Event event = Event.builder()
                .eventName(eventRequest.eventName())
                .organizerId(eventRequest.organizerId())
                .eventType(eventRequest.eventType())
                .expectedAttendees(eventRequest.expectedAttendees())
                .startTime(eventRequest.startTime())
                .endTime(eventRequest.endTime())
                .roomId(roomId)
                .status("PENDING")
                .bookingId(bookingId)
                .build();

        eventRepository.save(event);
        return mapToEventResponse(event);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventById(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
        return mapToEventResponse(event);
    }

    @Override
    public void deleteEventById(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        try {
            bookingClient.deleteBooking(event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to delete booking for event {}: {}", eventId, e.getMessage());
        }

        eventRepository.deleteById(eventId);
    }

    @Override
    public void updateEventStatus(String eventId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
        event.setStatus(status);
        eventRepository.save(event);
    }

    @Override
    public List<EventResponse> getEventsByStatus(String status) {
        return eventRepository.findByStatus(status)
                .stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }
}
