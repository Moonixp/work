package org.turntabl.chatapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Chat {
    UUID id;
    UUID groupId;
    boolean isDirectChat;
    LocalDateTime createdAt;
    LocalDateTime deletedAt;
}
