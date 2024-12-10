package ca.gbc.approvalservice.controller;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<?> approveEvent(@RequestBody ApprovalRequest approvalRequest) {
        try {
            ApprovalResponse approvalResponse = approvalService.approveEvent(approvalRequest);
            return ResponseEntity.ok(approvalResponse);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        try {
            List<ApprovalResponse> approvals = approvalService.getAllApprovals();
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<?> getApprovalById(@PathVariable String approvalId) {
        try {
            ApprovalResponse approvalResponse = approvalService.getApprovalById(approvalId);
            return ResponseEntity.ok(approvalResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getApprovalsByStatus(@PathVariable String status) {
        try {
            List<ApprovalResponse> approvals = approvalService.getApprovalsByStatus(status);
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}
