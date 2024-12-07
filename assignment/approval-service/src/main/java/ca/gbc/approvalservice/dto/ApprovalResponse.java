package ca.gbc.approvalservice.dto;

public record ApprovalResponse(String id,
                               String userId,
                               String eventId,
                               boolean isApproved,
                               String comments) {
}

