package org.turntabl.chatapp.dto.chat;

import lombok.Data;

@Data
public class AddMemberRequest {
    public String chatId;
    public String userId;
}
