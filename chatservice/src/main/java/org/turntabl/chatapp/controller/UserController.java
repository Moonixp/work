package org.turntabl.chatapp.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.turntabl.chatapp.dto.user.UserResponse;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.security.SecurityUtils;
import org.turntabl.chatapp.service.UserService;
import org.turntabl.chatapp.util.ChatAppUtils;

import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SecurityUtils securityUtils;
    private final ModelMapper modelMapper;
    private final UserService userService;

    public UserController(UserService userService, ModelMapper modelMapper, SecurityUtils securityUtils,
            ChatAppUtils chatAppUtils) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.securityUtils = securityUtils;
    }

    @GetMapping("")
    public ResponseEntity<?> getAllUsers() {
        var routeCheck = ChatAppUtils.ManagersOnlyRouteCheck(securityUtils);
        if (routeCheck.isPresent()) {
            return routeCheck.get();
        }
        try {
            return userService.getAllUsers()
                    .map(users -> {
                        List<UserResponse> response = users.stream()
                                .map(user -> modelMapper.map(user, UserResponse.class))
                                .collect(Collectors.toList());
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (ChatAppException ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "try again"));
        }
    }

    @GetMapping("/find")
    public ResponseEntity<?> findUserByEmail(@PathParam(value = "email") String email) {
        try {
            return userService.findByEmail(email)
                    .map(user -> ResponseEntity.ok(modelMapper.map(user, UserResponse.class)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (ChatAppException ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "try again"));
        }
    }

    @GetMapping("/id")
    public ResponseEntity<?> getUserbyId(@RequestParam String uuid) {

        return userService.findByUUid(UUID.fromString(uuid)).map(
                user -> ResponseEntity.ok().body(user)).orElse(ResponseEntity.notFound().build());

    }

}
