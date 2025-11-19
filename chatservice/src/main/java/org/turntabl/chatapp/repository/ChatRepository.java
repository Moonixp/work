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
import org.turntabl.chatapp.model.Chat;
import org.turntabl.chatapp.model.MyChatList;
import org.turntabl.chatapp.util.ChatAppUtils;

@Repository
public class ChatRepository {
    private final JdbcTemplate jdbcTemplate;

    public ChatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Chat> chatRowMapper = (rs, rowNum) -> {
        LocalDateTime createdAt = ChatAppUtils.convertTimestampToDate(rs.getTimestamp("created_at"));

        var builder = Chat.builder()
                .id(UUID.fromString(rs.getString("id")))
                .isDirectChat(rs.getBoolean("is_direct_chat"))
                .createdAt(createdAt)
                .deletedAt(null);
        String group_id = rs.getString("group_id");
        if (group_id != null) {
            builder.groupId(UUID.fromString(group_id));
        } else {
            builder.groupId(null);
        }
        return builder.build();
    };

    public Chat createChat(UUID groupId, boolean isDirectChat) {
        String query;
        if (isDirectChat) {
            query = "INSERT INTO chats(group_id, is_direct_chat) VALUES (null,true) RETURNING id";
        } else {
            query = "INSERT INTO chats(group_id, is_direct_chat) VALUES (?,false) RETURNING id";
        }

        try {
            UUID uuid;
            if (isDirectChat) {
                uuid = jdbcTemplate.queryForObject(query, UUID.class);
            } else {
                uuid = jdbcTemplate.queryForObject(query, UUID.class, groupId);
            }
            return find(uuid);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List<Chat> findAll() {
        var query = "SELECT id, group_id, is_direct_chat, created_at FROM chats WHERE deleted_at IS NULL";
        return jdbcTemplate.query(query, chatRowMapper);
    }

    public Chat find(UUID uuid) {
        var query = "SELECT id, group_id, is_direct_chat, created_at FROM chats WHERE deleted_at IS NULL AND id = ?";
        try {
            return jdbcTemplate.queryForObject(query, chatRowMapper, uuid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean delete(UUID uuid) {
        var query = "UPDATE chats SET deleted_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();
        Timestamp ts = Timestamp.valueOf(now);
        try {
            var rows = jdbcTemplate.update(query, ts, uuid);
            return rows > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean exists(UUID chatId) {
        String query = "SELECT EXISTS(SELECT 1 FROM chat_members WHERE id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, chatId);
    }

    public boolean chatIdExists(UUID chatId) {
        String query = "SELECT EXISTS(SELECT 1 FROM chats WHERE id = ?)";
        return jdbcTemplate.queryForObject(query, Boolean.class, chatId);
    }

    private final RowMapper<MyChatList> MyChatListRowMapper = (rs, rowNum) -> {
        return MyChatList.builder()
                .chatId((UUID) rs.getObject("chat_id"))
                .name(rs.getString("name"))
                .isGroup(rs.getBoolean("is_group"))
                .build();
    };

    public List<MyChatList> findAllChatsForUser(UUID userId) {
        final String query = """
                    WITH user_chats AS (
                        SELECT c.id AS chat_id,
                               c.is_direct_chat,
                               c.group_id
                        FROM chats c
                        JOIN chat_members cm ON cm.chat_id = c.id
                        WHERE cm.user_id = ?
                    ),
                    dm_names AS (
                        SELECT
                            uc.chat_id,
                            u.username AS name
                        FROM user_chats uc
                        JOIN chat_members cm ON cm.chat_id = uc.chat_id
                        JOIN users u ON u.id = cm.user_id
                        WHERE uc.is_direct_chat = true
                          AND cm.user_id <> ?
                    ),
                    group_names AS (
                        SELECT
                            uc.chat_id,
                            g.name
                        FROM user_chats uc
                        JOIN groups g ON g.id = uc.group_id
                        WHERE uc.is_direct_chat = false
                    )
                    SELECT
                        uc.chat_id,
                        COALESCE(g.name, d.name) AS name,
                        NOT uc.is_direct_chat AS is_group
                    FROM user_chats uc
                    LEFT JOIN group_names g ON g.chat_id = uc.chat_id
                    LEFT JOIN dm_names d ON d.chat_id = uc.chat_id
                    ORDER BY name;
                """;
        return jdbcTemplate.query(query, MyChatListRowMapper, userId, userId);
    }

    private final RowMapper<MyChatList> MyGroupListRowMapper = (rs, rowNum) -> {
        return MyChatList.builder()
                .chatId((UUID) rs.getObject("chat_id"))
                .name(rs.getString("name"))
                .isGroup(true) // all are groups
                .build();
    };

    public List<MyChatList> findAllGroupsForUser(UUID userId) {
        final String query = """
                    SELECT c.id AS chat_id,
                           g.name
                    FROM chats c
                    JOIN groups g ON g.id = c.group_id
                    JOIN group_members gm ON gm.group_id = g.id
                    WHERE gm.user_id = ?
                      AND c.is_direct_chat = false
                    ORDER BY g.name;
                """;

        return jdbcTemplate.query(query, MyGroupListRowMapper, userId);
    }
}
