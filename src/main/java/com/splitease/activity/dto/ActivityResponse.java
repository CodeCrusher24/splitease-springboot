package com.splitease.activity.dto;

import com.splitease.activity.ActivityLog;
import java.time.LocalDateTime;
import java.util.Map;

public record ActivityResponse(
        Long id,
        String action,
        Long actorId,
        Map<String, Object> payload,
        LocalDateTime createdAt
) {
    public static ActivityResponse from(ActivityLog log) {
        return new ActivityResponse(
                log.getId(),
                log.getAction(),
                log.getActor().getId(),
                log.getPayload(),
                log.getCreatedAt()
        );
    }
}