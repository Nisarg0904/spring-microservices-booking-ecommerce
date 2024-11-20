package ca.gbc.approvalservice.controller;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor

public class ApprovalController {
    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalResponse> approveEvent(@RequestBody ApprovalRequest approvalRequest) {
        ApprovalResponse approvalResponse = approvalService.approveEvent(approvalRequest);
        return ResponseEntity.ok(approvalResponse);
    }

    @GetMapping
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        List<ApprovalResponse> approvals = approvalService.getAllApprovals();
        return ResponseEntity.ok(approvals);
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ApprovalResponse> getApprovalById(@PathVariable String approvalId) {
        ApprovalResponse approvalResponse = approvalService.getApprovalById(approvalId);
        return ResponseEntity.ok(approvalResponse);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApprovalResponse>> getApprovalsByStatus(@PathVariable String status) {
        List<ApprovalResponse> approvals = approvalService.getApprovalsByStatus(status);
        return ResponseEntity.ok(approvals);
    }

}
