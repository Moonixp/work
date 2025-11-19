package org.turntabl.chatapp.dto.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateMessageRequest {
    String chatId;
    String content;
}
