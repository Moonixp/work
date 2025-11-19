package org.turntabl.chatapp.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.turntabl.chatapp.exception.ChatAppException;
import org.turntabl.chatapp.model.GroupApplications;
import org.turntabl.chatapp.repository.GroupApplicationRepository;

@Service
public class GroupApplicationsService {

    private final UserService userService;
    private final GroupService groupService;
    private final GroupApplicationRepository groupApplicationRepository;

    public GroupApplicationsService(UserService userService, GroupService groupService,
            GroupApplicationRepository groupApplicationRepository) {
        this.userService = userService;
        this.groupService = groupService;
        this.groupApplicationRepository = groupApplicationRepository;
    }

    public GroupApplications find(UUID groupApplicationId) throws ChatAppException {
        return groupApplicationRepository.find(groupApplicationId);
    }

    public GroupApplications find(UUID groupId, UUID userId) throws ChatAppException {
        if (!groupService.idExists(groupId)) {
            throw new ChatAppException("group does not exist");
        }
        if (!userService.idExists(userId)) {
            throw new ChatAppException("user does not exist");
        }

        return groupApplicationRepository.find(groupId, userId);
    }

    public List<GroupApplications> findByUser(UUID userId) throws ChatAppException {
        if (!userService.idExists(userId)) {
            throw new ChatAppException("user does not exist");
        }
        return groupApplicationRepository.findByUser(userId);
    }

    public List<GroupApplications> findAllbyGroup(UUID groupId) throws ChatAppException {
        if (!groupService.idExists(groupId)) {
            throw new ChatAppException("group does not exist");
        }
        return groupApplicationRepository.findAllbyGroup(groupId);

    }

    public GroupApplications create(String groupName, UUID userId) throws ChatAppException {
        if (!groupService.groupNameExists(groupName)) {
            throw new ChatAppException("group does not exist");
        }
        if (!userService.idExists(userId)) {
            throw new ChatAppException("user does not exist");
        }

        var groupId = groupService.find(groupName).getId();
        return groupApplicationRepository.create(groupId, userId);
    }

    public List<GroupApplications> findAll() throws ChatAppException {
        return groupApplicationRepository.findAll();
    }

    public boolean idExists(UUID id) {
        return groupApplicationRepository.idExists(id);
    }

    public boolean hasUserAppliedForGroup(UUID groupId, UUID userId) {
        return groupApplicationRepository.groupAndUserIdexists(groupId, userId);
    }
}
