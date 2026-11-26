package com.splitease.group.dto;

import com.splitease.group.GroupMember;

public record GroupMemberResponse(
        Long userId,
        String name,
        String email,
        String role
) {
    public static GroupMemberResponse from(GroupMember member) {
        return new GroupMemberResponse(
                member.getUser().getId(),
                member.getUser().getName(),
                member.getUser().getEmail(),
                member.getRole()
        );
    }
}