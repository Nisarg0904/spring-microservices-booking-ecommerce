package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.dto.EventResponse;
import ca.gbc.approvalservice.dto.EventRequest;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    @Value("${event.service.url}")
    private String eventServiceUrl;
    @Value("${user.service.url}")
    private String userServiceUrl;
    @Value("${booking.service.url}")
    private String bookingServiceUrl;

    private final ApprovalRepository approvalRepository;
    private final RestTemplate restTemplate;

    @Override
    public ApprovalResponse approveEvent(ApprovalRequest approvalRequest) {
        String userType = getUserType(approvalRequest.userId());
        if (!"staff".equalsIgnoreCase(userType)) {
            throw new IllegalStateException("Only staff members can approve or reject events.");
        }
        if (approvalRepository.findByEventId(approvalRequest.eventId()) != null) {
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
        return mapToApprovalResponse(savedApproval);
    }

    @Override
    public List<ApprovalResponse> getAllApprovals() {
        return approvalRepository.findAll()
                .stream()
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalResponse getApprovalById(String approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found with id: " + approvalId));
        return mapToApprovalResponse(approval);
    }

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

    private String getUserType(String userId) {
        String url = userServiceUrl+"/api/users/"+userId+"/type";
        return restTemplate.getForObject(url, String.class);
    }

    private void updateEventStatus(String eventId, String status) {
        String url = eventServiceUrl+"/api/events/"+eventId+"/status";
        restTemplate.patchForObject(url, new EventRequest(status), Void.class);
    }

    private EventResponse getEventById(String eventId) {
        String url = eventServiceUrl+"/api/events/"+eventId;
        ResponseEntity<EventResponse> response = restTemplate.getForEntity(url, EventResponse.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new IllegalStateException("Failed to fetch event details");
        }
    }

    private void deleteBooking(String bookingId) {
        String url = bookingServiceUrl+"/api/bookings/"+bookingId;
        restTemplate.delete(url);
    }
}
