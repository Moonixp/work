package org.turntabl.chatapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private String passwordHash;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
}
