package org.turntabl.users.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.turntabl.users.exception.InvalidTokenException;
import org.turntabl.users.exception.UserException;
import org.turntabl.users.model.User;
import org.turntabl.users.model.UserInfo;
import org.turntabl.users.repository.UserRepository;
import org.turntabl.users.security.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserService {
    private final UserRepository repository;
    private final JwtService jwtService;

    public UserService(UserRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    public Optional<List<User>> getAllUsers() throws UserException {
        return Optional.ofNullable(repository.findAll());
    }

    public boolean idExists(UUID userId) {
        return repository.idExists(userId);
    }

    public boolean emailExists(String email) {
        return repository.emailExists(email);
    }

    public boolean usernameExists(String username) {
        return repository.usernameExists(username);
    }

    public Optional<User> findByUsername(String username) throws UserException {
        return Optional.ofNullable(repository.findByUsername(username));
    }

    public User findByEmail(String email) throws UserException {
        return repository.findByEmail(email);
    }

    public Optional<User> findByUUid(UUID uuid) {
        return find(uuid);
    }

    public Optional<User> find(UUID id) {
        return Optional.ofNullable(repository.findByUUID(id));
    }

    public User create(String email, String passwordHash, String username, String role) throws UserException {
        return repository.create(email, passwordHash, username, role);
    }

    // ---------------- SECURITY ----------------- //
    private String extractTokenFromRequest(HttpServletRequest request) throws InvalidTokenException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            var token = authHeader.substring(7);
            if (token == null || !jwtService.isTokenValid(token)) {
                throw new InvalidTokenException("Invalid Access Token");
            }
            return token;
        }
        throw new InvalidTokenException("Invalid Access Token");
    }

    public UserInfo getUserInfoFromHttpRequest(HttpServletRequest request) throws InvalidTokenException {
        try {
            return getUserInfoFromToken(extractTokenFromRequest(request));
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException(ex.getMessage());
        }
    }

    public UserInfo getUserInfoFromToken(String token) {
        if (token != null && jwtService.isTokenValid(token)) {
            return UserInfo.builder()
                    .email(jwtService.extractUsername(token))
                    .role(jwtService.extractRole(token))
                    .userId(jwtService.extractUserId(token))
                    .build();
        }
        return null;
    }

}
