package org.turntabl.auth.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.turntabl.auth.exception.AuthException;
import org.turntabl.auth.model.User;
import org.turntabl.auth.repository.AuthRepository;
import org.turntabl.auth.security.JwtService;

@Service
public class AuthService {
    private final AuthRepository repository;
    private final JwtService jwtService;

    public AuthService(AuthRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    public Optional<List<User>> getAllUsers() throws AuthException {
        return Optional.ofNullable(repository.findAll());
    }

    public boolean idExists(UUID userId) {
        return repository.idExists(userId);
    }

    public boolean emailExists(String email) {
        return repository.emailExists(email);
    }

    public User findByEmail(String email) throws AuthException {
        return repository.findByEmail(email);
    }

    public Optional<User> findByUUid(UUID uuid) {
        return find(uuid);
    }

    public Optional<User> find(UUID id) {
        return Optional.ofNullable(repository.findByUUID(id));
    }

    public User create(String email, String passwordHash, UUID user_id, String role) throws AuthException {
        return repository.create(email, passwordHash, user_id, role);
    }

    /**
     * IssueToken will create and return jwt token based on standards
     */
    public String issueToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("userId", user.getUserId());
        return jwtService.generateToken(user.getEmail(), claims);
    }
}
