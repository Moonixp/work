package org.turntabl.chatapp.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.User;
import org.turntabl.chatapp.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<List<User>> getAllUsers() throws ChatAppException {
        return Optional.ofNullable(userRepository.findAll());
    }

    public boolean idExists(UUID userId) {
        return userRepository.idExists(userId);
    }

    public boolean emailExists(String email) {
        return userRepository.emailExists(email);
    }

    public boolean usernameExists(String username) {
        return userRepository.usernameExists(username);
    }

    public Optional<User> findByUsername(String username) throws ChatAppException {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<User> findByEmail(String email) throws ChatAppException {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public Optional<User> findByUUid(UUID uuid) {
        return find(uuid);
    }

    public Optional<User> find(UUID id) {
        return Optional.ofNullable(userRepository.findByUUID(id));
    }

}
