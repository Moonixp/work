package org.turntabl.chatapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.Group;
import org.turntabl.chatapp.util.ChatAppUtils;

@Repository
public class GroupRepository {

    private final JdbcTemplate jdbcTemplate;

    public GroupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Group> groupRowMapper = (rs, rowNum) -> {
        LocalDateTime createdAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("created_at"));

        return Group.builder()
                .id(UUID.fromString(rs.getString("id")))
                .ownerId(UUID.fromString(rs.getString("owner_id")))
                .name(rs.getString("name"))
                .createdAt(createdAt)
                .build();

    };

    public Group find(UUID groupId) throws ChatAppException {
        String query = "select id, name, created_at, owner_id FROM groups WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(query, groupRowMapper, groupId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("no group found by this Id: ");
        }
    }

    public Group find(String groupName) throws ChatAppException {
        String query = "select id, name, created_at, owner_id FROM groups WHERE name = ?";
        try {
            return jdbcTemplate.queryForObject(query, groupRowMapper, groupName);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("no group found by this name: ");
        }
    }

    public Group create(String groupName, UUID ownerId) throws ChatAppException {
        UUID groupUUID;
        var query = "insert into groups(name,owner_id)  values(?,?) returning id";
        try {
            groupUUID = jdbcTemplate.queryForObject(query, UUID.class, groupName, ownerId);
            if (groupUUID == null)
                throw new ChatAppException("Failed to create group");
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to create group Data Access Error: " + ex.getMessage());
        }
        return find(groupUUID);
    }

    public List<Group> findAll() throws ChatAppException {
        String query = "SELECT id, name, created_at, owner_id FROM groups";
        try {
            return jdbcTemplate.query(query, groupRowMapper);
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to get groups Data Access Error: " + ex.getMessage());
        }
    }

    public boolean exists(UUID id) {
        String query = "SELECT EXISTS(SELECT 1 FROM groups  WHERE id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, id);
    }

    public boolean exists(String name) {
        String query = "SELECT EXISTS(SELECT 1 FROM groups  WHERE name = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, name);
    }

}