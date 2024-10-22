package ca.gbc.eventservice.service;

import ca.gbc.eventservice.dto.BookingRequest;
import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.model.Event;
import ca.gbc.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8087/api/users/{Id}/role";
    private static final String ROOM_SERVICE_URL = "http://localhost:8086/api/rooms/{Id}/capacity";
    private static final String BOOKING_SERVICE_URL = "http://localhost:8088/api/bookings";


    @Override
    public EventResponse createEvent(EventRequest eventRequest) {
        String organizerRole = getOrganizerRole(eventRequest.organizerId());

        if ("student".equalsIgnoreCase(organizerRole) && eventRequest.expectedAttendees() > 25) {
            throw new IllegalArgumentException("Students cannot organize events with more than 25 attendees.");
        }
        if (eventRequest.expectedAttendees() > getRoomCapacity(eventRequest.roomId())) {
            throw new IllegalArgumentException("Room capacity exceeded.");
        }
        try {
            makeBookingForEvent(eventRequest.organizerId(), eventRequest.roomId(), eventRequest.startTime(), eventRequest.endTime());
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
                .build();

        // Save the event in the database
        Event savedEvent = eventRepository.save(event);

        // Return an EventResponse using the saved event data
        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getEventName(),
                savedEvent.getOrganizerId(),
                savedEvent.getEventType(),
                savedEvent.getExpectedAttendees(),
                savedEvent.getRoomId(),
                savedEvent.getStartTime(),
                savedEvent.getEndTime(),
                savedEvent.getStatus()
        );
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getEventName(),
                        event.getOrganizerId(),
                        event.getEventType(),
                        event.getExpectedAttendees(),
                        event.getRoomId(),
                        event.getStartTime(),
                        event.getEndTime(),
                        event.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventById(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getOrganizerId(),
                event.getEventType(),
                event.getExpectedAttendees(),
                event.getRoomId(),
                event.getStartTime(),
                event.getEndTime(),
                event.getStatus()
        );
    }

    @Override
    public void deleteEventById(String eventId) {
        eventRepository.deleteById(eventId);

    }

    private String getOrganizerRole(String organizerId) {
        String url = USER_SERVICE_URL.replace("{Id}", organizerId);
        return restTemplate.getForObject(url, String.class);
    }

    private int getRoomCapacity(String roomId) {
        String url = ROOM_SERVICE_URL.replace("{Id}", roomId);
        return restTemplate.getForObject(url, Integer.class);
    }

    private void makeBookingForEvent(String userId, String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        BookingRequest bookingRequest = new BookingRequest(userId, roomId, startTime, endTime, "Event Booking");
        restTemplate.postForObject(BOOKING_SERVICE_URL, bookingRequest, Void.class);
    }
}
