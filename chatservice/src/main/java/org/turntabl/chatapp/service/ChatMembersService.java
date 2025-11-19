package org.turntabl.chatapp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.ChatMembers;
import org.turntabl.chatapp.repository.ChatMembersRepository;

@Service
public class ChatMembersService {
    private final ChatMembersRepository chatMembersRepository;

    public ChatMembersService(ChatMembersRepository chatMembersRepository) {
        this.chatMembersRepository = chatMembersRepository;
    }

    public List<ChatMembers> findAllByChatId(UUID chatId) throws ChatAppException {
        return chatMembersRepository.findAllByChatId(chatId);
    }

    public List<ChatMembers> findAllByUserId(UUID userId) throws ChatAppException {
        return chatMembersRepository.findAllByUserId(userId);
    }

    public ChatMembers create(UUID chatId, UUID userId) throws ChatAppException {

        return chatMembersRepository.create(chatId, userId);
    }

    public ChatMembers findByUserIdChatId(UUID userId, UUID chatId) throws ChatAppException {
        return findByUserIdChatId(userId, chatId);
    }

    public List<ChatMembers> findAll() throws ChatAppException {
        return chatMembersRepository.findAll();
    }

    public ChatMembers addMember(UUID chatId, UUID userId) throws ChatAppException {
        return create(chatId, userId);
    }

    public boolean removeMember(UUID chatId, UUID userId) {
        return removeMember(chatId, userId);
    }

    public boolean chatIdExists(UUID userId) {
        return chatMembersRepository.chatIdExists(userId);
    }

    public boolean userIdExists(UUID chatId) {
        return chatMembersRepository.chatIdExists(chatId);
    }

    public boolean userIdAndChatIdExists(UUID userId, UUID chatId) {
        return chatMembersRepository.userIdAndChatIdExists(userId, chatId);
    }

    public List<ChatMembers> findOtherChatMembers(UUID userId, UUID chatId) throws ChatAppException {
        return chatMembersRepository.findOtherChatMembers(userId, chatId);
    }

    public boolean dmExists(UUID user1, UUID user2) {
        return chatMembersRepository.dmExists(user1, user2);
    }
}
