package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final RestTemplate restTemplate;

    private final BookingRepository bookingRepository;

    private static final String ROOM_SERVICE_URL = "http://localhost:8086/api/rooms/{roomId}/availability";


    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        if (!isRoomAvailable(bookingRequest.roomId(), bookingRequest.startTime().toString(), bookingRequest.endTime().toString())) {
            throw new IllegalStateException("Room is not available for the selected time range");
        }
        Booking booking = Booking.builder()
                .userId(bookingRequest.userId())
                .roomId(bookingRequest.roomId())
                .startTime(bookingRequest.startTime())
                .endTime(bookingRequest.endTime())
                .purpose(bookingRequest.purpose())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return new BookingResponse(
                savedBooking.getId(),
                savedBooking.getUserId(),
                savedBooking.getRoomId(),
                savedBooking.getStartTime(),
                savedBooking.getEndTime(),
                savedBooking.getPurpose()
        );
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(booking -> new BookingResponse(
                        booking.getId(),
                        booking.getUserId(),
                        booking.getRoomId(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        booking.getPurpose()))
                .collect(Collectors.toList());    }

    @Override
    public BookingResponse getBookingById(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
        );    }

    @Override
    public boolean isRoomAvailable(String roomId, String startTime, String endTime) {
        String url = ROOM_SERVICE_URL.replace("{roomId}", roomId);
        try {
            Boolean isAvailable = restTemplate.getForObject(url, Boolean.class);

            if (Boolean.TRUE.equals(isAvailable)) {
                LocalDateTime start = LocalDateTime.parse(startTime);
                LocalDateTime end = LocalDateTime.parse(endTime);
                List<Booking> conflictingBookings = bookingRepository.findByRoomIdAndStartTimeBetweenOrEndTimeBetween(
                        roomId, start, end, start, end);
                conflictingBookings.add(bookingRepository.findByRoomIdAndStartTimeAndEndTime(roomId, start, end));
                return conflictingBookings.isEmpty();
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("RoomService returned error: " + e.getStatusCode());
        } catch (RestClientException e) {
            System.err.println("RoomService is unavailable: " + e.getMessage());
        }
        // Default to false if RoomService is unavailable or returns an error
        return false;
    }

    @Override
    public void deleteBookingById(String bookingId) {
        bookingRepository.deleteById(bookingId);
    }
}
