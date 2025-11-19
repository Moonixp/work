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
import org.turntabl.chatapp.util.ChatAppUtils;
import org.turntabl.chatapp.model.GroupApplications;

@Repository
public class GroupApplicationRepository {
    private final JdbcTemplate jdbcTemplate;

    public GroupApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GroupApplications> GroupApplicationRowMapper = (rs, rowNum) -> {
        LocalDateTime appliedAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("applied_at"));
        LocalDateTime approvedAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("approved_at"));

        return GroupApplications.builder()
                .id(UUID.fromString(rs.getString("id")))
                .groupId(UUID.fromString(rs.getString("group_id")))
                .userId(UUID.fromString(rs.getString("user_id")))
                .status(rs.getString("status"))
                .appliedAt(appliedAt)
                .approvedAt(approvedAt)
                .build();
    };

    public GroupApplications find(UUID groupApplicationId) throws ChatAppException {
        String query = "SELECT id, group_id, user_id, status, applied_at, approved_at  FROM group_applications WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(query, GroupApplicationRowMapper, groupApplicationId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("No Group Application found by this Id: ");
        }
    }

    public GroupApplications find(UUID groupId, UUID userId) throws ChatAppException {
        String query = "SELECT id, group_id, user_id, status, applied_at, approved_at  FROM group_applications WHERE group_id = ? AND user_id = ?";
        try {
            return jdbcTemplate.queryForObject(query, GroupApplicationRowMapper, groupId, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException(
                    "No Group Application found by this userId: " + userId + " and groupId " + groupId);
        }
    }

    public List<GroupApplications> findByUser(UUID userId) throws ChatAppException {
        String query = "SELECT id, group_id, user_id, status, applied_at, approved_at  FROM group_applications WHERE  user_id = ?";
        try {
            return jdbcTemplate.query(query, GroupApplicationRowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("No Group Application found by this userId: " + userId);
        }
    }

    public List<GroupApplications> findAllbyGroup(UUID groupId) throws ChatAppException {
        String query = "SELECT id, group_id, user_id, status, applied_at, approved_at  FROM group_applications WHERE  user_id = ?";
        try {
            return jdbcTemplate.query(query, GroupApplicationRowMapper, groupId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("No Group Application found for this group: " + groupId);
        }
    }

    public GroupApplications create(UUID groupId, UUID userId) throws ChatAppException {
        UUID groupApplicationId;
        String query = "insert into group_applications values( group_id, user_id)  values (?,?) returning id";
        try {
            groupApplicationId = jdbcTemplate.queryForObject(query, UUID.class, groupId, userId);
            if (groupApplicationId == null)
                throw new ChatAppException("failed to create an application for the group: ");
        } catch (DataAccessException ex) {
            throw new ChatAppException(
                    "failed to create an application for the group" + "\n" + ex.getMessage());
        }
        return find(groupApplicationId);
    }

    public List<GroupApplications> findAll() throws ChatAppException {
        String query = "SELECT id, group_id, user_id, status, applied_at, approved_at FROM group_applications";
        try {
            return jdbcTemplate.query(query, GroupApplicationRowMapper);
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to get groups applications Data Access Error: " + ex.getMessage());
        }
    }

    public boolean idExists(UUID id) {
        String query = "SELECT EXISTS(SELECT 1 FROM group_applications  WHERE id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, id);
    }

    public boolean groupAndUserIdexists(UUID groupId, UUID userId) {
        String query = "SELECT EXISTS(SELECT 1 FROM group_applications  WHERE group_id = ? AND user_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, groupId, userId);
    }

}
