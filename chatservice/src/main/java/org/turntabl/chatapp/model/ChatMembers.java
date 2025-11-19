package org.turntabl.chatapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ChatMembers {
    UUID chatId;
    UUID userId;
    LocalDateTime joinedAt;
    LocalDateTime leftAt;
}
