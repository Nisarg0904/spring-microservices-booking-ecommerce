package ca.gbc.eventservice.service;

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
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    @Value("${room.service.url}")
    private String roomServiceUrl;
    @Value("${user.service.url}")
    private String userServiceUrl;
    @Value("${booking.service.url}")
    private String bookingServiceUrl;

    private final EventRepository eventRepository;
    private final RestTemplate restTemplate;

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
        String organizerType = getOrganizerType(eventRequest.organizerId());
        String bookingId;

        if ("student".equalsIgnoreCase(organizerType) && eventRequest.expectedAttendees() > 25) {
            log.error("Event creation failed: Students cannot organize events with more than 25 attendees.");

            throw new IllegalArgumentException("Students cannot organize events with more than 25 attendees.");
        }
        if (eventRequest.expectedAttendees() > getRoomCapacity(eventRequest.roomId())) {
            log.error("Event creation failed: Room capacity exceeded.");

            throw new IllegalArgumentException("Room capacity exceeded.");
        }
        try {
            bookingId = makeBookingForEvent(eventRequest.organizerId(), eventRequest.roomId(), eventRequest.startTime(), eventRequest.endTime());
        } catch (Exception e) {
            throw new IllegalArgumentException("Booking failed: " + e.getMessage());
        }
        Event event = Event.builder()
                .eventName(eventRequest.eventName())
                .organizerId(eventRequest.organizerId())
                .eventType(eventRequest.eventType())
                .expectedAttendees(eventRequest.expectedAttendees())
                .startTime(eventRequest.startTime())
                .endTime(eventRequest.endTime())
                .roomId(eventRequest.roomId())
                .Status("PENDING")
                .bookingId(bookingId)
                .build();

        eventRepository.save(event);
        return mapToEventResponse(event);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToEventResponse) // Use the mapping method here
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
        deleteBooking(event.getBookingId());
        eventRepository.deleteById(eventId);
    }

    @Override
    public void updateEventStatus(String eventId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));
        event.setStatus(status);
        eventRepository.save(event);
    }

    private void deleteBooking(String bookingId) {
        String url = bookingServiceUrl + "/api/bookings/" + bookingId;
        restTemplate.delete(url);
    }

    private String getOrganizerType(String organizerId) {
        String url = userServiceUrl+"/api/users/"+organizerId+"/type";
        return restTemplate.getForObject(url, String.class);
    }

    private int getRoomCapacity(String roomId) {
        String url = roomServiceUrl+"/api/rooms/"+roomId+"/capacity" ;
        return restTemplate.getForObject(url, Integer.class);
    }

    private String makeBookingForEvent(String userId, String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        BookingRequest bookingRequest = new BookingRequest(userId, roomId, startTime, endTime, "Event Booking");
        try {
            ResponseEntity<BookingResponse> response = restTemplate.postForEntity(bookingServiceUrl+"/api/bookings", bookingRequest, BookingResponse.class);
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                return response.getBody().id();
            } else {
                throw new IllegalStateException("Failed to create booking: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error during booking creation", e);
            throw new IllegalStateException("Error during booking creation:"+e.getMessage());
        }
    }
}



