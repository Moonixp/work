package org.turntabl.chatapp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.User;
import org.turntabl.chatapp.util.ChatAppUtils;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        var createdAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("created_at"));

        return User.builder()
                .id(UUID.fromString(rs.getString("id")))
                .role(rs.getString("role"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .createdAt(createdAt)
                .deletedAt(null)
                .build();
    };

    private final RowMapper<User> userAuthRowMapper = (rs, rowNum) -> {
        var createdAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("created_at"));
        return User.builder()
                .id(UUID.fromString(rs.getString("id")))
                .role(rs.getString("role"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .createdAt(createdAt)
                .deletedAt(null)
                .build();
    };

    public List<User> findAll() {
        String query = "select id, role, username, email, created_at FROM users WHERE deleted_at IS NULL";
        return jdbcTemplate.query(query, userRowMapper);
    }

    public User findByUsername(String username) throws ChatAppException {
        String query = "SELECT id, role, username, email, created_at FROM users WHERE deleted_at IS NULL AND username = ?";
        try {
            return jdbcTemplate.queryForObject(query, userRowMapper, username);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("Data access error: " + e.getMessage());
        }
    }

    public User findByEmail(String email) throws ChatAppException {
        String query = "SELECT id, role, username, email, created_at FROM users WHERE deleted_at IS NULL AND email = ?";
        try {
            return jdbcTemplate.queryForObject(query, userRowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("Data access error: " + e.getMessage());
        }
    }

    public User findByUUID(UUID uuid) {
        String query = "SELECT id, role, username, email, created_at FROM users WHERE deleted_at IS NULL AND id = ?";

        return jdbcTemplate.queryForObject(query, userRowMapper, uuid);

    }

    public User findByEmailAuth(String email) throws ChatAppException {
        String query = "SELECT id, role, username, email, password_hash, created_at FROM users WHERE deleted_at IS NULL AND email = ?";
        try {
            return jdbcTemplate.queryForObject(query, userAuthRowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("no user found by email: " + email);
        }
    }

    public User create(String email, String passwordHash, String username, String role) throws ChatAppException {
        var query = "INSERT into users(username,role, email,password_hash) values (?,?,?,?) RETURNING id";
        try {
            UUID uuid = jdbcTemplate.queryForObject(query, UUID.class, username,
                    role,
                    email,
                    passwordHash);
            return findByUUID(uuid);
        } catch (DataAccessException ex) {
            throw new ChatAppException("could not create User");
        }
    }

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
}
