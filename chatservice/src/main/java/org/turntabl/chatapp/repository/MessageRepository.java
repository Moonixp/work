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
import org.turntabl.chatapp.model.Message;
import org.turntabl.chatapp.util.ChatAppUtils;

@Repository
public class MessageRepository {
    private final JdbcTemplate jdbcTemplate;

    public MessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Message> MessageRowMapper = (rs, rowNum) -> {
        LocalDateTime created_at = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("created_at"));
        LocalDateTime deleted_at = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("deleted_at"));
        LocalDateTime updatedAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("deleted_at"));

        return Message.builder()
                .id(UUID.fromString(rs.getString("id")))
                .senderId(UUID.fromString(rs.getString("sender_id")))
                .chatId(UUID.fromString(rs.getString("chat_id")))
                .content(rs.getString("content"))
                .createdAt(created_at)
                .updatedAt(updatedAt)
                .deletedAt(deleted_at)
                .build();
    };

    public List<Message> findByUserAndChat(UUID chatId, UUID userId) throws ChatAppException {
        String query = "SELECT id, sender_id, chat_id, content , created_at, deleted_at, updated_at FROM messages WHERE  sender_id = ? AND chat_id = ?";
        try {
            return jdbcTemplate.query(query, MessageRowMapper, userId, chatId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("data access error");
        }
    }

    public Message find(UUID id) throws ChatAppException {
        String query = "SELECT id, sender_id, chat_id, content , created_at, deleted_at, updated_at FROM messages WHERE  id = ?";
        try {
            return jdbcTemplate.queryForObject(query, MessageRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException(
                    "No message found for this id " + id);
        }
    }

    public List<Message> findAllByChat(UUID chatId) throws ChatAppException {
        String query = "SELECT id, sender_id, chat_id, content , created_at, deleted_at, updated_at FROM messages WHERE  chat_id = ?";
        try {
            return jdbcTemplate.query(query, MessageRowMapper, chatId);
        } catch (EmptyResultDataAccessException e) {
            throw new ChatAppException("No message found for this chat: " + chatId);
        }
    }

    public Message create(UUID chatId, UUID senderId, String content) throws ChatAppException {
        try {
            String query = "insert into messages values(chat_id,sender_id,content)  values (?,?,?) returning id";
            UUID id = jdbcTemplate.queryForObject(query, UUID.class, chatId, senderId, content);
            if (id == null) {
                throw new ChatAppException("failed to create the message");
            }
            return find(id);
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to create message " + "\n" + ex.getMessage());
        }
    }

    public List<Message> findAll() throws ChatAppException {
        String query = "SELECT id, sender_id, chat_id, created_at, updated_at, deleted_at FROM messages";
        try {
            return jdbcTemplate.query(query, MessageRowMapper);
        } catch (DataAccessException ex) {
            throw new ChatAppException("failed to get messages Data Access Error: " + ex.getMessage());
        }
    }

    public boolean idExists(UUID chatId) {
        String query = "SELECT EXISTS(SELECT 1 FROM messages  WHERE sender_id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, chatId);
    }
}
