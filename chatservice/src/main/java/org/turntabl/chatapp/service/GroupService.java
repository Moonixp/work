package org.turntabl.chatapp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.Group;
import org.turntabl.chatapp.repository.GroupRepository;

@Service
public class GroupService {

    private final UserService userService;
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository, UserService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;

    }

    public Group find(UUID groupId) throws ChatAppException {
        return groupRepository.find(groupId);
    }

    public Group find(String groupName) throws ChatAppException {
        return groupRepository.find(groupName);
    }

    public Group create(String groupName, UUID ownerId) throws ChatAppException {
        if (!userService.idExists(ownerId)) {
            throw new ChatAppException("user does not exist");
        }

        Group group = groupRepository.create(groupName, ownerId);
        return group;
    }

    public List<Group> findAll() throws ChatAppException {
        return groupRepository.findAll();
    }

    public boolean idExists(UUID groupId) {
        return groupRepository.exists(groupId);
    }

    public boolean groupNameExists(String groupName) {
        return groupRepository.exists(groupName);
    }
}
