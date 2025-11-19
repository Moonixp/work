package org.turntabl.chatapp.service;

import java.util.List;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.Chat;

import org.turntabl.chatapp.model.MyChatList;
import org.turntabl.chatapp.model.User;
import org.turntabl.chatapp.repository.ChatRepository;

@Service
public class ChatService {

    private final UserService userService;

    private final ChatRepository chatRepository;
    private final GroupService groupService;

    private final ChatMembersService chatMembersService;

    public ChatService(ChatRepository chatRepository, GroupService groupService,
            UserService userService,
            ChatMembersService chatMembersService) {
        this.chatRepository = chatRepository;
        this.groupService = groupService;

        this.userService = userService;
        this.chatMembersService = chatMembersService;
    }

    public Optional<Chat> createChat(UUID groupId, boolean isDirectChat) throws ChatAppException {
        if (groupId != null) {
            if (!groupService.idExists(groupId)) {
                throw new ChatAppException("could not find group");
            }
        }
        return Optional.ofNullable(chatRepository.createChat(groupId, isDirectChat));
    }

    /// creates a dm by adding 2 members to a created chat
    /// should be handled in chatMembers
    public boolean createDirectMessage(UUID firstUserId, UUID secondUserId) throws ChatAppException {
        var optional = createChat(null, true);
        if (optional.isPresent()) {
            var chat = optional.get();
            try {
                var id = chatMembersService.addMember(chat.getId(), firstUserId);
                if (!chatMembersService.chatIdExists(id.getChatId())) {
                    return false;
                }
                id = chatMembersService.addMember(chat.getId(), secondUserId);
                if (!chatMembersService.chatIdExists(id.getChatId())) {
                    return false;
                }
                return true;
            } catch (ChatAppException ex) {
                return false;
            }
        }
        return false;
    }

    public Optional<List<Chat>> findAll() {
        return Optional.ofNullable(chatRepository.findAll());
    }

    public Optional<Chat> find(UUID uuid) {
        return Optional.ofNullable(chatRepository.find(uuid));
    }

    public boolean delete(UUID uuid) {
        return chatRepository.delete(uuid);
    }

    public boolean removeMember(UUID chatId, UUID userId) throws ChatAppException {
        if (!chatIdExists(chatId)) {
            throw new ChatAppException("chat does not exist");
        }

        if (!userService.idExists(userId)) {
            throw new ChatAppException("user does not exist");
        }

        return chatMembersService.removeMember(chatId, userId);
    }

    public Optional<List<User>> getMemberList(UUID chatId) throws ChatAppException {
        if (!chatRepository.chatIdExists(chatId)) {
            throw new ChatAppException("chat not found");
        }
        // return Optional.ofNullable(chatRepository.getMemberList(chatId));
        return Optional.empty();
    }

    public boolean chatIdExists(UUID chatId) {
        return chatRepository.chatIdExists(chatId);
    }

    public List<MyChatList> getMyChats(UUID userId) {
        return chatRepository.findAllChatsForUser(userId);
    }

    public List<MyChatList> getMygroups(UUID userId) {
        return chatRepository.findAllGroupsForUser(userId);
    }
}
