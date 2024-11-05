package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    @Value("${room.service.url}")
    private String roomServiceUrl;
    @Value("${user.service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

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
        if (!isUserValid(bookingRequest.userId())) {
            throw new IllegalStateException("User does not exist");
        }
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




    private boolean isUserValid(String userId) {
        String url =userServiceUrl+"/api/users/"+userId;
        try {
            restTemplate.getForObject(url, Void.class);
            return true;
        } catch (RestClientException e) {
            System.err.println("UserService is unavailable or user does not exist: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isRoomAvailable(String roomId, String startTime, String endTime) {
        String url = roomServiceUrl+"/api/rooms/"+roomId+"/availability";
        try {
            Boolean isAvailable = restTemplate.getForObject(url, Boolean.class);

            if (Boolean.TRUE.equals(isAvailable)) {
                LocalDateTime start = LocalDateTime.parse(startTime);
                LocalDateTime end = LocalDateTime.parse(endTime);
                List<Booking> conflictingBookings = bookingRepository.findByRoomIdAndStartTimeBetweenOrEndTimeBetween(
                        roomId, start, end, start, end);
                Booking booking= bookingRepository.findByRoomIdAndStartTimeAndEndTime(roomId, start, end);
                if(booking!=null) {
                    conflictingBookings.add(booking);
                }
                return conflictingBookings.isEmpty();
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("RoomService returned error: " + e.getStatusCode());
        } catch (RestClientException e) {
            System.err.println("RoomService is unavailable: " + e.getMessage());
        }
        return false;
    }


}
