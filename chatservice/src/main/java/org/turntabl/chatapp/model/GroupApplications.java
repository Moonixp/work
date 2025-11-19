package org.turntabl.chatapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupApplications {
    private UUID id;
    private UUID groupId;
    private UUID userId;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
}
