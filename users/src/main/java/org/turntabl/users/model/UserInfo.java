package org.turntabl.users.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserInfo {
    private UUID userId;
    private String email;
    private String role;
}