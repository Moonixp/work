package org.turntabl.auth.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private UUID userId;
    private String email;
    private String role;
    private String passwordHash;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
}
