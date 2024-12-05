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
            String userType = userClient.getUserType(approvalRequest.userId());
            if (!"staff".equalsIgnoreCase(userType)) {
                throw new IllegalStateException("Only staff members can approve or reject events.");
            }

            if (approvalRepository.findByEventId(approvalRequest.eventId()) != null) {
                throw new IllegalStateException("This event has already been approved or rejected.");
            }

            EventResponse event = eventClient.getEventById(approvalRequest.eventId());

            Approval approval = Approval.builder()
                    .eventId(approvalRequest.eventId())
                    .userId(approvalRequest.userId())
                    .isApproved(approvalRequest.isApproved())
                    .comments(approvalRequest.comments())
                    .build();

            if (approvalRequest.isApproved()) {
                eventClient.updateEventStatus(approvalRequest.eventId(), new EventRequest("APPROVED"));
            } else {
                eventClient.updateEventStatus(approvalRequest.eventId(), new EventRequest("REJECTED"));
                bookingClient.deleteBooking(event.bookingId());
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
    }
