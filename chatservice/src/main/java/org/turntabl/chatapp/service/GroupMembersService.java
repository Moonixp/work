package org.turntabl.chatapp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.GroupMembers;
import org.turntabl.chatapp.repository.GroupMembersRepository;

@Service
public class GroupMembersService {

    GroupMembersRepository groupMembersRepository;
    UserService userService;
    GroupService groupService;

    public GroupMembersService(GroupMembersRepository groupMembersRepository, UserService userService,
            GroupService groupService) {
        this.groupMembersRepository = groupMembersRepository;
        this.userService = userService;
        this.groupService = groupService;

    }

    public GroupMembers find(UUID groupMemberId) throws ChatAppException {
        return groupMembersRepository.find(groupMemberId).get(0);
    }

    public GroupMembers create(UUID groupId, UUID userId) throws ChatAppException {
        if (!userService.idExists(userId)) {
            throw new ChatAppException("User does not exist");
        }

        if (!groupService.idExists(groupId)) {
            throw new ChatAppException("Group does not exist");
        }

        return groupMembersRepository.create(groupId, userId);
    }

    public GroupMembers addMember(UUID groupId, UUID userId) throws ChatAppException {
        return create(groupId, userId);
    }

    public List<GroupMembers> findAll() throws ChatAppException {
        return groupMembersRepository.findAll();
    }

    public boolean exists(UUID id) {
        return groupMembersRepository.exists(id);
    }

    public boolean userExists(UUID userId) {
        return groupMembersRepository.exists(userId);
    }
}
