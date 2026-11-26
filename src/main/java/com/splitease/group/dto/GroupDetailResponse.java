package com.splitease.group.dto;

import com.splitease.group.Group;

import java.time.LocalDateTime;
import java.util.List;

public record GroupDetailResponse(
        Long id,
        String name,
        String description,
        String currency,
        Long createdBy,
        LocalDateTime createdAt,
        boolean archived,
        int memberCount,
        List<GroupMemberResponse> members
) {
    public static GroupDetailResponse from(Group group, List<GroupMemberResponse> members) {
        return new GroupDetailResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCurrency(),
                group.getCreatedBy().getId(),
                group.getCreatedAt(),
                group.isArchived(),
                members.size(),
                members
        );
    }
}