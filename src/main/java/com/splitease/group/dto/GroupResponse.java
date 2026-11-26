package com.splitease.group.dto;

import com.splitease.group.Group;

import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String name,
        String description,
        String currency,
        Long createdBy,
        LocalDateTime createdAt,
        boolean archived,
        int memberCount
) {
    public static GroupResponse from(Group group, int memberCount) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCurrency(),
                group.getCreatedBy().getId(),
                group.getCreatedAt(),
                group.isArchived(),
                memberCount
        );
    }
}