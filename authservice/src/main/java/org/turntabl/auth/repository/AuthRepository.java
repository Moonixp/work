package org.turntabl.auth.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.turntabl.auth.exception.AuthException;
import org.turntabl.auth.model.User;
import org.turntabl.auth.utils.Utils;

@Repository
public class AuthRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {

        var createdAt = Utils.convertTimestampToDate(rs.getTimestamp("created_at"));
        return User.builder()
                .userId(UUID.fromString(rs.getString("user_id")))
                .role(rs.getString("role"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .createdAt(createdAt)
                .deletedAt(null)
                .build();
    };

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User create(String email, String pasword_hash, UUID userId, String role) throws AuthException {
        var query = "INSERT into users(user_id,role,password_hash,email) values (?,?,?,?) RETURNING id";
        try {
            UUID uuid = jdbcTemplate.queryForObject(query, UUID.class,
                    userId.toString(),
                    role,
                    email,
                    pasword_hash);
            return findByUUID(uuid);
        } catch (DataAccessException ex) {
            throw new AuthException("could not create User");
        }
    }

    // Exists Methods
    public boolean idExists(UUID uuid) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE user_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, uuid);
    }

    public boolean emailExists(String email) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, email);
    }

    // Search Methods

    public List<User> findAll() {
        String query = "select user_id, role, email, created_at, FROM users WHERE deleted_at IS NULL";
        return jdbcTemplate.query(query, rowMapper);
    }

    public User findByEmail(String email) throws AuthException {
        String query = "SELECT user_id, role, email, created_at FROM users WHERE deleted_at IS NULL AND email = ?";
        try {
            return jdbcTemplate.queryForObject(query, rowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            throw new AuthException("no user found by email: " + email);
        }
    }

    public User findByUUID(UUID uuid) {
        String query = "SELECT user_id, role,  email, created_at FROM users WHERE deleted_at IS NULL AND user_id = ?";

        return jdbcTemplate.queryForObject(query, rowMapper, uuid);

    }
}
