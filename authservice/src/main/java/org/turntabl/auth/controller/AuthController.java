package org.turntabl.auth.controller;

import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.turntabl.auth.dto.LoginRequest;
import org.turntabl.auth.dto.LoginResponse;
import org.turntabl.auth.dto.RegisterRequest;
import org.turntabl.auth.exception.AuthException;
import org.turntabl.auth.model.User;
import org.turntabl.auth.service.AuthService;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;

@RestController
@RequestMapping("/api/auth")
class AuthController {
    private final AuthService service;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public AuthController(AuthService service, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.service = service;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid Email"));
        }

        if (!request.getEmail().endsWith("@turntabl.io")) {
            return ResponseEntity.badRequest().body(Map.of("error", "company Email only"));
        }

        if (!service.emailExists(request.getEmail())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        }

        User user;
        String dummyHash = "$2a$10$dummy.hash.to.prevent.timing.attacks";
        try {
            user = service.findByEmail(request.getEmail());
        } catch (AuthException ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }

        if (user != null) {
            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                var token = service.issueToken(user);

                var response = modelMapper.map(user, LoginResponse.class);
                response.setToken(token);

                return ResponseEntity.ok()
                        .body(response);

            }
            passwordEncoder.matches(request.getPassword(), dummyHash);
        }
        return ResponseEntity.status(401)
                .body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        var role = request.getRole();
        if (role == null || role.isEmpty()) {
            if (role != "USER" || role != "MANAGER") {
                role = "USER";
            }
        }

        if (request.getPassword().length() < 5 || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid password, should be 5 or more characters"));
        }

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid Email"));
        }

        if (!request.getEmail().endsWith("@turntabl.io")) {
            return ResponseEntity.badRequest().body(Map.of("error", "company Email only"));
        }

        if (service.emailExists(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        User user;
        try {
            // demo
            UUID user_id = new UUIDGenerator().generateId(1);
            /// make request here
            user = service.create(request.getEmail(), passwordHash, user_id, role);
        } catch (AuthException ex) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal Server Error, please try again later", "debug", ex.getMessage()));
        }

        if (user != null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(500)
                .body(Map.of("error", "Internal Server Error, please try again later"));

    }

}
