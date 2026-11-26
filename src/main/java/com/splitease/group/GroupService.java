package com.splitease.group;

import com.splitease.common.ForbiddenException;
import com.splitease.common.NotFoundException;
import com.splitease.group.dto.*;
import com.splitease.user.User;
import com.splitease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    // ----- Create -----

    @Transactional
    public GroupDetailResponse createGroup(GroupCreateRequest req, User creator) {
        Group group = Group.builder()
                .name(req.name())
                .description(req.description())
                .currency(req.currency())
                .createdBy(creator)
                .build();
        Group saved = groupRepository.save(group);

        GroupMember admin = GroupMember.builder()
                .group(saved)
                .user(creator)
                .role("admin")
                .build();
        groupMemberRepository.save(admin);

        return GroupDetailResponse.from(saved, List.of(GroupMemberResponse.from(admin)));
    }

    // ----- Read -----

    @Transactional(readOnly = true)
    public List<GroupResponse> listMyGroups(User currentUser) {
        List<Group> groups = groupRepository.findAllByUserId(currentUser.getId());
        return groups.stream()
                .map(g -> GroupResponse.from(g, (int) groupMemberRepository.countByGroupId(g.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getGroup(Long groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ForbiddenException("Not a member of this group");
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        List<GroupMemberResponse> memberResponses = members.stream()
                .map(GroupMemberResponse::from)
                .collect(Collectors.toList());

        return GroupDetailResponse.from(group, memberResponses);
    }

    // ----- Add member -----

    @Transactional
    public void addMember(Long groupId, AddMemberRequest req, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ForbiddenException("Only group members can add people");
        }

        User newUser = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new NotFoundException("User with this email not found"));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newUser.getId())) {
            throw new IllegalArgumentException("User is already a member");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(newUser)
                .role("member")
                .build();
        groupMemberRepository.save(member);
    }

    // ----- Remove member -----

    @Transactional
    public void removeMember(Long groupId, Long targetUserId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        GroupMember currentMembership = groupMemberRepository
                .findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));

        if (!"admin".equals(currentMembership.getRole())) {
            throw new ForbiddenException("Only admins can remove members");
        }

        if (targetUserId.equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot remove yourself");
        }

        GroupMember target = groupMemberRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new NotFoundException("Member not found in group"));

        groupMemberRepository.delete(target);
    }

    // ----- Leave group -----

    @Transactional
    public void leaveGroup(Long groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        if (group.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Group creators cannot leave. You must delete the group instead.");
        }

        GroupMember membership = groupMemberRepository
                .findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));

        // NOTE: balance check will be added in Phase 6 (after expenses exist)
        groupMemberRepository.delete(membership);
    }
}