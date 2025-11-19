package org.turntabl.chatapp.repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.GroupMembers;
import org.turntabl.chatapp.util.ChatAppUtils;

@Repository
public class GroupMembersRepository {

    private final JdbcTemplate jdbcTemplate;

    public GroupMembersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GroupMembers> groupMembersRowMapper = (rs, rowNum) -> {
        LocalDateTime createdAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("created_at"));

        return GroupMembers.builder()
                .groupId(UUID.fromString(rs.getString("id")))
                .userId(UUID.fromString(rs.getString("user_id")))
                .createdAt(createdAt)
                .build();

    };

    public List<GroupMembers> find(UUID groupId) throws ChatAppException {
        String query = "SELECT group_id, user_id, created_at FROM group_members WHERE group_id = ? AND deleted_at IS NULL";
        try {
            return jdbcTemplate.query(query, groupMembersRowMapper, groupId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList(); // or throw if you prefer
        }
    }

    public GroupMembers create(UUID groupId, UUID userId) throws ChatAppException {
        UUID groupMembersUUID;
        var query = "insert into group_members(group_id,user_id)  values(?,?) returning group_id";
        try {
            groupMembersUUID = jdbcTemplate.queryForObject(query, UUID.class, groupId, userId);
            if (groupMembersUUID == null)
                throw new ChatAppException("Failed to insert a group member");
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to insert a group member Data Access Error: " + ex.getMessage());
        }
        return find(groupMembersUUID).getFirst();
    }

    public List<GroupMembers> findAll() throws ChatAppException {
        String query = "SELECT group_id, user_id, created_at, role_id FROM group_members";
        try {
            return jdbcTemplate.query(query, groupMembersRowMapper);
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to get all group members Data Access Error: " + ex.getMessage());
        }
    }

    public boolean exists(UUID id) {
        String query = "SELECT EXISTS(SELECT 1 FROM  group_members  WHERE group_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, id);
    }

    public boolean userExists(UUID userId) {
        String query = "SELECT EXISTS(SELECT 1 FROM  group_members  WHERE user_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, userId);
    }
}