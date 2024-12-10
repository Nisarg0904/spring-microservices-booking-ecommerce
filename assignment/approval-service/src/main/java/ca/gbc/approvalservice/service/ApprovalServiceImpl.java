package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.client.BookingClient;
import ca.gbc.approvalservice.client.EventClient;
import ca.gbc.approvalservice.client.UserClient;
import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.dto.EventResponse;
import ca.gbc.approvalservice.dto.EventRequest;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;


    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class ApprovalServiceImpl implements ApprovalService {

        private final ApprovalRepository approvalRepository;
        private final BookingClient bookingClient;
        private final EventClient eventClient;
        private final UserClient userClient;

        @Override
        public ApprovalResponse approveEvent(ApprovalRequest approvalRequest) {
            // Fetch user type with Circuit Breaker applied in UserClient
            String userType = userClient.getUserType(approvalRequest.userId());

            // Ensure only staff can approve or reject events
            if (!"staff".equalsIgnoreCase(userType)) {
                log.warn("User with ID {} is not authorized to approve events. User type: {}", approvalRequest.userId(), userType);
                throw new IllegalStateException("Only staff members can approve or reject events.");
            }

            // Prevent duplicate approvals or rejections
            if (approvalRepository.findByEventId(approvalRequest.eventId()) != null) {
                log.warn("Event with ID {} has already been approved or rejected.", approvalRequest.eventId());
                throw new IllegalStateException("This event has already been approved or rejected.");
            }

            // Fetch event details
            EventResponse event = eventClient.getEventById(approvalRequest.eventId());

            // Build the approval entity
            Approval approval = Approval.builder()
                    .eventId(approvalRequest.eventId())
                    .userId(approvalRequest.userId())
                    .isApproved(approvalRequest.isApproved())
                    .comments(approvalRequest.comments())
                    .build();

            // Update event and booking status based on approval
            if (approvalRequest.isApproved()) {
                log.info("Approving event with ID {}", approvalRequest.eventId());
                eventClient.updateEventStatus(approvalRequest.eventId(), new EventRequest("APPROVED"));
            } else {
                log.info("Rejecting event with ID {}", approvalRequest.eventId());
                eventClient.updateEventStatus(approvalRequest.eventId(), new EventRequest("REJECTED"));
                bookingClient.deleteBooking(event.bookingId());
            }

            // Save the approval
            Approval savedApproval = approvalRepository.save(approval);
            log.info("Approval saved for event ID: {}", approvalRequest.eventId());

            return mapToApprovalResponse(savedApproval);
        }

        @Override
        public List<ApprovalResponse> getAllApprovals() {
            log.info("Fetching all approvals...");
            return approvalRepository.findAll()
                    .stream()
                    .map(this::mapToApprovalResponse)
                    .collect(Collectors.toList());
        }

        @Override
        public ApprovalResponse getApprovalById(String approvalId) {
            log.info("Fetching approval with ID: {}", approvalId);
            Approval approval = approvalRepository.findById(approvalId)
                    .orElseThrow(() -> new IllegalArgumentException("Approval not found with id: " + approvalId));
            return mapToApprovalResponse(approval);
        }

        @Override
        public List<ApprovalResponse> getApprovalsByStatus(String status) {
            log.info("Fetching approvals with status: {}", status);
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
    }