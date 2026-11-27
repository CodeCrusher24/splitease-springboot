package com.splitease.settlement.dto;

import com.splitease.settlement.Settlement;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        Long groupId,
        Long fromUser,
        Long toUser,
        Long amountMinor,
        LocalDateTime createdAt
) {
    public static SettlementResponse from(Settlement s) {
        return new SettlementResponse(
                s.getId(),
                s.getGroup().getId(),
                s.getFromUser().getId(),
                s.getToUser().getId(),
                s.getAmountMinor(),
                s.getCreatedAt()
        );
    }
}