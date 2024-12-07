package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.client.RoomClient;
import ca.gbc.bookingservice.client.UserClient;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {



    private final RoomClient roomClient ;

    private final UserClient userClient ;

    private final BookingRepository bookingRepository;

    private BookingResponse mapToBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
        );
    }

    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        // Validate the user
        if (!isUserValid(bookingRequest.userId())) {
            throw new IllegalStateException("User does not exist");
        }

        // Handle room assignment logic
        String roomId = bookingRequest.roomId();

        if (roomId == null || roomId.isEmpty()) {
            if (bookingRequest.capacity() > 0) {
                log.info("Room ID not provided. Attempting to find a suitable room for capacity: {}", bookingRequest.capacity());

                // Fetch available rooms with sufficient capacity
                List<String> availableRoomIds = roomClient.getAvailableRoomIds(bookingRequest.capacity());
                log.info("Available room IDs fetched: {}", availableRoomIds);

                // Check for availability and assign the first suitable room
                for (String availableRoomId : availableRoomIds) {
                    if (isRoomAvailable(availableRoomId, bookingRequest.startTime().toString(), bookingRequest.endTime().toString())) {
                        roomId = availableRoomId;
                        log.info("Assigned roomId {} to the booking request", roomId);
                        break;
                    }
                }

                if (roomId == null || roomId.isEmpty()) {
                    throw new IllegalStateException("No suitable room available for the requested capacity and time.");
                }
            } else {
                throw new IllegalArgumentException("Room ID or capacity must be provided for booking.");
            }
        } else {
            // Room ID is provided: Validate its availability
            if (!isRoomAvailable(roomId, bookingRequest.startTime().toString(), bookingRequest.endTime().toString())) {
                throw new IllegalStateException("Room is not available for the selected time range.");
            }
        }

        // Proceed with booking creation
        Booking booking = Booking.builder()
                .userId(bookingRequest.userId())
                .roomId(roomId)
                .startTime(bookingRequest.startTime())
                .endTime(bookingRequest.endTime())
                .purpose(bookingRequest.purpose())
                .build();

        bookingRepository.save(booking);

        return mapToBookingResponse(booking);
    }


    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookingResponse> getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .map(this::mapToBookingResponse);  // Map found booking to response
    }


    @Override
    public void deleteBookingById(String bookingId) {
        bookingRepository.deleteById(bookingId);
    }




    @Override
    public boolean isRoomAvailable(String roomId, String startTime, String endTime) {
        log.info("Calling RoomClient to check room availability for roomId: {}", roomId);
        Boolean isAvailable = roomClient.isRoomAvailable(roomId); // Circuit breaker applies here
        log.info("Room Service responded for roomId {}: {}", roomId, isAvailable);

        if (Boolean.TRUE.equals(isAvailable)) {
            LocalDateTime start = LocalDateTime.parse(startTime.trim());
            LocalDateTime end = LocalDateTime.parse(endTime.trim());
            List<Booking> conflictingBookings = bookingRepository.findByRoomIdAndStartTimeBetweenOrEndTimeBetween(
                    roomId, start, end, start, end);
            Booking booking = bookingRepository.findByRoomIdAndStartTimeAndEndTime(roomId, start, end);
            if (booking != null) {
                conflictingBookings.add(booking);
            }
            return conflictingBookings.isEmpty();
        }
        return false;
    }

    public boolean isUserValid(String userId) {
        return userClient.getUserById(userId) != null; // Circuit breaker applies here
    }

}