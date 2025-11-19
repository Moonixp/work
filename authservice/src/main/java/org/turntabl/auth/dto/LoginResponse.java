package org.turntabl.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String user_id;
    private String email;
    private String role;
    private String token;
}
