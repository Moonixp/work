package org.turntabl.chatapp.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.ChatMembers;
import org.turntabl.chatapp.util.ChatAppUtils;

@Repository
public class ChatMembersRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatMembersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ChatMembers> chatMembersRowMapper = (rs, rowNum) -> {
        LocalDateTime joinedAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("joined_at"));
        LocalDateTime leftAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("left_at"));

        return ChatMembers.builder()
                .chatId(UUID.fromString(rs.getString("chat_id")))
                .userId(UUID.fromString(rs.getString("user_id")))
                .joinedAt(joinedAt)
                .leftAt(leftAt)
                .build();

    };

    public List<ChatMembers> findAllByChatId(UUID chatId) throws ChatAppException {
        String query = "select chat_id, user_id, joined_at, left_at FROM chat_members WHERE chat_id = ?";
        try {
            return jdbcTemplate.query(query, chatMembersRowMapper, chatId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("no chat found by this Id: ");
        }
    }

    public List<ChatMembers> findAllByUserId(UUID userId) throws ChatAppException {
        String query = "select chat_id, user_id, joined_at, left_at FROM chat_members WHERE user_id = ?";
        try {
            return jdbcTemplate.query(query, chatMembersRowMapper, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("no chat found by this Id: ");
        }
    }

    public List<ChatMembers> findOtherChatMembers(UUID userId, UUID chatId) {
        String query = """
                    SELECT chat_id, user_id, joined_at, left_at
                    FROM chat_members
                    WHERE user_id <> ?
                      AND chat_id = ?
                """;
        List<ChatMembers> members = jdbcTemplate.query(
                query, chatMembersRowMapper, userId, chatId);
        return members;
    }

    public ChatMembers findByUserIdChatId(UUID userId, UUID chatId) {
        String query = "select chat_id, user_id, joined_at, left_at FROM chat_members WHERE user_id = ? AND chat_id = ?";
        return jdbcTemplate.queryForObject(query, chatMembersRowMapper, userId, chatId);
    }

    public ChatMembers create(UUID chatId, UUID userId) throws ChatAppException {
        UUID chatMemberId;
        var query = "insert into chat_members(chat_id,user_id)  values(?,?) returning chat_id";
        try {
            chatMemberId = jdbcTemplate.queryForObject(query, UUID.class, chatId, userId);
            if (chatMemberId == null)
                throw new ChatAppException("Failed to create chat");
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to create chat Data Access Error: " + ex.getMessage());
        }
        return findByUserIdChatId(userId, chatId);
    }

    public List<ChatMembers> findAll() throws ChatAppException {
        String query = "select chat_id, user_id, joined_at, left_at FROM chat_members";
        try {
            return jdbcTemplate.query(query, chatMembersRowMapper);
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to get chat Data Access Error: " + ex.getMessage());
        }
    }

    public boolean removeMember(UUID chatId, UUID userId) {
        var query = "UPDATE chat_members SET left_at = ? WHERE chat_id = ? AND user_id =?";

        LocalDateTime now = LocalDateTime.now();
        Timestamp ts = Timestamp.valueOf(now);
        try {
            var rows = jdbcTemplate.update(query, ts, chatId, userId);
            return rows > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean userIdExists(UUID userId) {
        String query = "SELECT EXISTS(SELECT 1 FROM chat_members  WHERE user_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, userId);
    }

    public boolean chatIdExists(UUID chatId) {
        String query = "SELECT EXISTS(SELECT 1 FROM chat_members  WHERE chat_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, chatId);
    }

    public boolean userIdAndChatIdExists(UUID userId, UUID chatId) {
        String query = "SELECT EXISTS(SELECT 1 FROM chat_members  WHERE chat_id = ? AND user_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, chatId, userId);
    }

    public boolean dmExists(UUID user1, UUID user2) {
        String sql = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM chats c
                        JOIN chat_members cm1 ON cm1.chat_id = c.id
                        JOIN chat_members cm2 ON cm2.chat_id = c.id
                        WHERE c.is_direct_chat = true
                          AND cm1.user_id = ?
                          AND cm2.user_id = ?
                    );
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                user1,
                user2);
    }

}
