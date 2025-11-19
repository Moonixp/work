package org.turntabl.users.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.turntabl.users.dto.UserResponse;
import org.turntabl.users.exception.InvalidTokenException;
import org.turntabl.users.exception.UserException;
import org.turntabl.users.model.UserInfo;
import org.turntabl.users.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ModelMapper modelMapper;
    private final UserService service;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.service = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        UserInfo claims;
        try {
            claims = service.getUserInfoFromHttpRequest(request);
        } catch (InvalidTokenException ex) {
            return ResponseEntity.status(403).body(Map.of("error", "unauthorized"));
        }
        if (claims.getRole() != "MANAGER") {
            return ResponseEntity.status(403).body(Map.of("message", "unauthorized: Managers only"));
        }
        // check if user is manaager
        try {
            return service.getAllUsers()
                    .map(users -> {
                        List<UserResponse> response = users.stream()
                                .map(user -> modelMapper.map(user, UserResponse.class))
                                .collect(Collectors.toList());
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (UserException ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "try again"));
        }
    }

    @GetMapping("/email")
    public ResponseEntity<?> findUserByEmail(@PathParam(value = "email") String email, HttpServletRequest request) {
        try {
            if (!service.emailExists(email)) {
                return ResponseEntity.notFound().build();
            }
            var user = service.findByEmail(email);
            return ResponseEntity.ok(modelMapper.map(user, UserResponse.class));
        } catch (UserException ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "try again"));
        }
    }

    @GetMapping("/id")
    public ResponseEntity<?> getUserbyId(@RequestParam String uuid, HttpServletRequest request) {
        UUID id = UUID.fromString(uuid);
        if (!service.idExists(id)) {
            ResponseEntity.notFound().build();
        }
        return service.findByUUid(id).map(
                user -> ResponseEntity.ok().body(user)).orElse(ResponseEntity.notFound().build());

    }

    @GetMapping("/username")
    public ResponseEntity<?> getUserbyUsername(@RequestParam String username, HttpServletRequest request) {
        if (!service.usernameExists(username)) {
            ResponseEntity.notFound().build();
        }
        try {
            return service.findByUsername(username).map(
                    user -> ResponseEntity.ok().body(user)).orElse(ResponseEntity.notFound().build());
        } catch (UserException ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "try again"));
        }

    }

}
