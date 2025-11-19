package org.turntabl.chatapp.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.turntabl.chatapp.dto.chat.AddMemberRequest;
import org.turntabl.chatapp.dto.chat.CreateDmRequest;
import org.turntabl.chatapp.dto.chat.CreateGroupRequest;
import org.turntabl.chatapp.dto.chat.CreateMessageRequest;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.exception.GlobalExceptionHandler;
import org.turntabl.chatapp.model.ChatMembers;
import org.turntabl.chatapp.model.Message;
import org.turntabl.chatapp.model.User;
import org.turntabl.chatapp.security.SecurityUtils;
import org.turntabl.chatapp.service.*;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final GroupMembersService groupMembersService;

    private final ChatMembersService chatMembersService;

    private final MessageService messageService;
    private final SecurityUtils securityUtils;
    private final GroupService groupService;
    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService,
            GlobalExceptionHandler globalExceptionHandler, SecurityUtils securityUtils, GroupService groupService,
            MessageService messageService, ChatMembersService chatMembersService,
            GroupMembersService groupMembersService) {
        this.chatService = chatService;

        this.securityUtils = securityUtils;
        this.groupService = groupService;
        this.messageService = messageService;
        this.chatMembersService = chatMembersService;
        this.userService = userService;
        this.groupMembersService = groupMembersService;

    }

    @PostMapping("/create/dm")
    public ResponseEntity<?> createDirectMessage(@RequestBody CreateDmRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "no user_id found"));
        }
        if (request.getUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "no user_id found"));
        }
        var userId = securityUtils.getCurrentUserId();
        if (userId.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Invalid User"));
        }

        UUID firstUserId = userId.get();
        UUID secondUserId = UUID.fromString(request.getUserId());

        if (secondUserId.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid user_id"));
        }

        if (chatMembersService.dmExists(firstUserId, secondUserId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "dm already exists"));
        }

        try {

            if (chatService.createDirectMessage(firstUserId, secondUserId)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.internalServerError().build();
            }
        } catch (ChatAppException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/create/group")
    public ResponseEntity<?> createGroupChat(@RequestBody CreateGroupRequest request) {
        // should be in the group controller
        var userId = securityUtils.getCurrentUserId();
        if (userId.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Invalid User"));
        }

        var ownerId = userId.get();

        if (!securityUtils.isManager()) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden: only managers can create groups"));
        }

        if (request.getGroupName() == null || request.getGroupName().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty group name"));
        }

        if (groupService.groupNameExists(request.getGroupName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "group name already exists"));
        }

        try {
            var group = groupService.create(request.getGroupName(), ownerId);
            if (group == null) {
                return ResponseEntity.badRequest().body(Map.of("error", " failed to create group"));
            }
            chatService.createChat(group.getId(), false);
            var member = groupMembersService.addMember(group.getId(), ownerId);
            if (member == null) {
                return ResponseEntity.badRequest().body(Map.of("error", " failed to add member"));
            }

            return ResponseEntity.ok(group);
        } catch (ChatAppException ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/mychats")
    public ResponseEntity<?> getAllMyChats() {
        var userId = securityUtils.getCurrentUserId();
        if (userId.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Invalid User"));
        }
        var chats = chatService.getMyChats(userId.get()).stream().collect(Collectors.toList());
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/mygroups")
    public ResponseEntity<?> getAllMyGroups() {
        var userId = securityUtils.getCurrentUserId();
        if (userId.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Invalid User"));
        }
        var chats = chatService.getMygroups(userId.get()).stream().collect(Collectors.toList());
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/id")
    public ResponseEntity<?> getChatById(@RequestParam String id) {
        return chatService.find(UUID.fromString(id)).map(
                ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteChatById(@RequestParam String id) {
        return chatService.delete(UUID.fromString(id)) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/members/add")
    public ResponseEntity<?> postMethodName(@RequestBody AddMemberRequest request) {
        if (request.getChatId().isEmpty() || request.getUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "chat_id and user_id is required"));
        }
        UUID chatId = UUID.fromString(request.getChatId());
        UUID userId = UUID.fromString(request.getUserId());
        if (userId.toString().isEmpty() || chatId.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid chat_id and user_id"));
        }
        try {
            return Optional.ofNullable(chatMembersService.addMember(chatId, userId)).map(
                    data -> ResponseEntity.ok().body(data)).orElse(
                            ResponseEntity.notFound().build());
        } catch (ChatAppException ex) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @DeleteMapping("/members/remove")
    public ResponseEntity<?> removeMemberFromChat(@RequestBody AddMemberRequest request) {
        if (request.getChatId().isEmpty() || request.getUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "chat_id and user_id  is required"));
        }

        UUID chatId = UUID.fromString(request.getChatId());
        UUID userId = UUID.fromString(request.getUserId());

        if (userId.toString().isEmpty() || chatId.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid chat_id and user_id"));
        }
        return chatMembersService.removeMember(chatId, userId) ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/members")
    public ResponseEntity<?> getMembersList(@RequestParam String id) {
        UUID chatId = UUID.fromString(id);
        if (chatId.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid chat_id"));
        }
        if (!chatService.chatIdExists(chatId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "chat_id does not exist"));
        }

        try {
            List<ChatMembers> members = chatMembersService.findAllByChatId(chatId);
            List<User> users = members.stream()
                    .map(member -> {
                        try {
                            return userService.find(member.getUserId())
                                    .orElseThrow(
                                            () -> new ChatAppException("no user found for id " + member.getUserId()));
                        } catch (ChatAppException e) {
                            return null;
                        }
                    })
                    .toList();
            return ResponseEntity.ok(users);
        } catch (ChatAppException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/message/create")
    public ResponseEntity<?> createAMessageForAChat(@RequestBody CreateMessageRequest request) {

        if (!request.getChatId().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty chat_id"));
        }
        if (!request.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty content"));
        }
        UUID chatId = UUID.fromString(request.getChatId());
        if (chatId.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid chat_id"));
        }
        Optional<UUID> userId = securityUtils.getCurrentUserId();
        if (userId.isPresent()) {
            try {
                var message = messageService.create(chatId, userId.get(), request.getContent());
                return ResponseEntity.ok(message);
            } catch (ChatAppException ex) {
                return ResponseEntity.internalServerError().build();
            }
        }

        return ResponseEntity.internalServerError().build();
    }

    @GetMapping("/message/all")
    public ResponseEntity<?> getMethodName(@RequestParam String chatId) {

        if (!chatId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty chat_id"));
        }
        UUID chatUUID = UUID.fromString(chatId);
        if (chatUUID.toString().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid chat_id"));
        }

        if (!chatService.chatIdExists(chatUUID)) {
            return ResponseEntity.badRequest().body(Map.of("error", "chat does not exist"));
        }
        UUID userId;
        if (securityUtils.getCurrentUserId().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "chat does not exist"));
        }
        userId = securityUtils.getCurrentUserId().get();

        if (!chatMembersService.userIdAndChatIdExists(userId, chatUUID)) {
            return ResponseEntity.status(403).body(Map.of("error", "user doesn't have access to chat"));
        }

        try {
            List<Message> messages = messageService.findAllByChat(chatUUID);
            return ResponseEntity.ok(messages);
        } catch (ChatAppException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
