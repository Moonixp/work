package org.turntabl.users.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.turntabl.users.exception.UserException;
import org.turntabl.users.model.User;
import org.turntabl.users.utils.Utils;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {

        var createdAt = Utils.convertTimestampToDate(rs.getTimestamp("created_at"));
        return User.builder()
                .id(UUID.fromString(rs.getString("id")))
                .role(rs.getString("role"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .createdAt(createdAt)
                .deletedAt(null)
                .build();
    };

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User create(String email, String passwordHash, String username, String role) throws UserException {
        var query = "INSERT into users(username,role, email,password_hash) values (?,?,?,?) RETURNING id";
        try {
            UUID uuid = jdbcTemplate.queryForObject(query, UUID.class, username,
                    role,
                    email,
                    passwordHash);
            return findByUUID(uuid);
        } catch (DataAccessException ex) {
            throw new UserException("could not create User");
        }
    }

    // Exists Methods
    public boolean idExists(UUID uuid) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, uuid);
    }

    public boolean usernameExists(String username) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, username);
    }

    public boolean emailExists(String email) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, email);
    }

    // Search Methods

    public List<User> findAll() {
        String query = "select id, role, username, email, password_hash, created_at FROM users WHERE deleted_at IS NULL";
        return jdbcTemplate.query(query, rowMapper);
    }

    public User findByEmail(String email) throws UserException {
        String query = "SELECT id, role, username, email, password_hash, created_at FROM users WHERE deleted_at IS NULL AND email = ?";
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            throw new UserException("no user found by email: " + email);
        }
    }

    public User findByUUID(UUID uuid) {
        String query = "SELECT id, role, username, password_hash, email, created_at FROM users WHERE deleted_at IS NULL AND id = ?";

        return jdbcTemplate.queryForObject(query, rowMapper, uuid);

    }

    public User findByUsername(String username) throws UserException {
        String query = "SELECT id, role, username,password_hash, email, created_at FROM users WHERE deleted_at IS NULL AND username = ?";
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, username);
        } catch (EmptyResultDataAccessException e) {
            throw new UserException("Data access error: " + e.getMessage());
        }
    }
}
