package org.turntabl.chatapp.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyChatList {
    private UUID chatId;
    private String name;
    private boolean isGroup;
}
