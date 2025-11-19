package org.turntabl.chatapp.dto.chat;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatResponse {
    UUID id;
    UUID groudId;
    boolean isDirectChat;
    LocalDateTime createdAt;
}