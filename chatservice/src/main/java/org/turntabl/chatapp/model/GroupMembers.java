package org.turntabl.chatapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupMembers {
    UUID groupId;
    UUID userId;
    LocalDateTime createdAt;
}
