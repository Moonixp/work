package org.turntabl.chatapp.dto.chat;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyChatsResponse {
    String name;
    boolean isGroup;
    UUID chatId;
}
