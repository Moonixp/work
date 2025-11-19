package org.turntabl.users.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
}
