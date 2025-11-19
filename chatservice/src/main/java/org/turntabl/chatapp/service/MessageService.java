package org.turntabl.chatapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.Message;
import org.turntabl.chatapp.repository.MessageRepository;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChatService chatService;

    public MessageService(MessageRepository messageRepository, UserService userService, ChatService chatService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.chatService = chatService;
    }

    public Message find(UUID id) throws ChatAppException {
        return messageRepository.find(id);
    }

    public List<Message> findAllByChat(UUID chatId) throws ChatAppException {
        if (!chatService.chatIdExists(chatId)) {
            return new ArrayList<>();
        }
        return messageRepository.findAllByChat(chatId);
    }

    public Message create(UUID chatId, UUID senderId, String content) throws ChatAppException {
        if (!chatService.chatIdExists(chatId)) {
            throw new ChatAppException("chat not found");
        }

        if (!userService.idExists(senderId)) {
            throw new ChatAppException("user not found");
        }
        if (!content.isEmpty()) {
            throw new ChatAppException("content is empty");

        }

        return messageRepository.create(chatId, senderId, content);

    }

    public List<Message> findAll() throws ChatAppException {
        return messageRepository.findAll();
    }

    public boolean idExists(UUID chatId) {
        return messageRepository.idExists(chatId);
    }
}
