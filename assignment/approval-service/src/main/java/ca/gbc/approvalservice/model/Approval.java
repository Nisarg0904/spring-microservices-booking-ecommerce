package ca.gbc.approvalservice.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "approvals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Approval {
    @Id
    private String id;
    private String userId;
    private String eventId;
    private boolean isApproved;
    private String comments;
}
