package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.dto.EventResponse;
import ca.gbc.approvalservice.dto.EventRequest;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final RestTemplate restTemplate;

    private static final String EVENT_SERVICE_URL = "http://localhost:8089/api/events/{eventId}/status";
    private static final String USER_SERVICE_URL = "http://localhost:8087/api/users/{userId}/role";
    private static final String BOOKING_SERVICE_URL = "http://localhost:8088/api/bookings/{bookingId}";

    @Override
    public ApprovalResponse approveEvent(ApprovalRequest approvalRequest) {
        String userRole = getUserRole(approvalRequest.userId());
        if (!"staff".equalsIgnoreCase(userRole)) {
            throw new IllegalStateException("Only staff members can approve or reject events.");
        }
        if (approvalRepository.findByEventId(approvalRequest.eventId())!=null) {
            throw new IllegalStateException("This event has already been approved or rejected.");
        }
        EventResponse event = getEventById(approvalRequest.eventId());

        Approval approval = Approval.builder()
                .eventId(approvalRequest.eventId())
                .userId(approvalRequest.userId())
                .isApproved(approvalRequest.isApproved())
                .comments(approvalRequest.comments())
                .build();

        if (approvalRequest.isApproved()) {
            updateEventStatus(approvalRequest.eventId(), "APPROVED");
        } else {
            updateEventStatus(approvalRequest.eventId(), "REJECTED");
            deleteBooking(event.bookingId());
        }
        Approval savedApproval = approvalRepository.save(approval);
        return new ApprovalResponse(
                savedApproval.getId(),
                savedApproval.getUserId(),
                savedApproval.getEventId(),
                savedApproval.isApproved(),
                savedApproval.getComments()
        );
    }

    @Override
    public List<ApprovalResponse> getAllApprovals() {
        return approvalRepository.findAll()
                .stream()
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());    }

    @Override
    public ApprovalResponse getApprovalById(String approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found with id: " + approvalId));
        return mapToApprovalResponse(approval);    }

    @Override
    public List<ApprovalResponse> getApprovalsByStatus(String status) {
        if (status.equalsIgnoreCase("approved")) {
            return approvalRepository.findByIsApproved(true)
                    .stream()
                    .map(this::mapToApprovalResponse)
                    .collect(Collectors.toList());
        }

            return approvalRepository.findByIsApproved(false)
                    .stream()
                    .map(this::mapToApprovalResponse)
                    .collect(Collectors.toList());

    }





    private ApprovalResponse mapToApprovalResponse(Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getUserId(),
                approval.getEventId(),
                approval.isApproved(),
                approval.getComments()
        );
    }

    private String getUserRole(String userId) {
        String url = USER_SERVICE_URL.replace("{userId}", userId);
        return restTemplate.getForObject(url, String.class);
    }
    private void updateEventStatus(String eventId, String Status) {
        String url = EVENT_SERVICE_URL.replace("{eventId}", eventId);
            restTemplate.patchForObject(url, new EventRequest(Status), Void.class);
    }
    private EventResponse getEventById(String eventId) {
        String url = EVENT_SERVICE_URL.replace("{eventId}/status", eventId);
        ResponseEntity<EventResponse> response = restTemplate.getForEntity(url, EventResponse.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new IllegalStateException("Failed to fetch event details");
        }
    }
    private void deleteBooking(String bookingId) {
        String url = BOOKING_SERVICE_URL.replace("{bookingId}", bookingId);
        restTemplate.delete(url);
    }
}
