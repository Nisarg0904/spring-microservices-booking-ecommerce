package ca.gbc.approvalservice.dto;

public record ApprovalRequest(  String userId,
         String eventId,
         boolean isApproved,
         String comments) {
}
