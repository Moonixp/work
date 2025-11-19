package org.turntabl.chatapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Group {
    UUID id;
    String name;
    UUID ownerId;
    LocalDateTime createdAt;
}
