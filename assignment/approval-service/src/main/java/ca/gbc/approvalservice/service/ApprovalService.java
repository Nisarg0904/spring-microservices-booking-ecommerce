package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;

import java.util.List;

public interface ApprovalService {
    ApprovalResponse approveEvent(ApprovalRequest approvalRequest);

    List<ApprovalResponse> getAllApprovals();

    ApprovalResponse getApprovalById(String approvalId);

    List<ApprovalResponse> getApprovalsByStatus(String status);

}
