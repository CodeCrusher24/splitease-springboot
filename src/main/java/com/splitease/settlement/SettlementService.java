package com.splitease.settlement;

import com.splitease.activity.ActivityLog;
import com.splitease.activity.ActivityLogRepository;
import com.splitease.common.ForbiddenException;
import com.splitease.common.NotFoundException;
import com.splitease.group.Group;
import com.splitease.group.GroupMemberRepository;
import com.splitease.group.GroupRepository;
import com.splitease.settlement.dto.SettlementCreateRequest;
import com.splitease.settlement.dto.SettlementResponse;
import com.splitease.user.User;
import com.splitease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    public SettlementService(
            SettlementRepository settlementRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            ActivityLogRepository activityLogRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public SettlementResponse createSettlement(Long groupId, SettlementCreateRequest req, User currentUser) {
        // 1. Verify both are members
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ForbiddenException("Not a member of this group");
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, req.toUser())) {
            throw new IllegalArgumentException("Recipient is not in this group");
        }
        if (req.toUser().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot pay yourself");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        User recipient = userRepository.findById(req.toUser())
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        // 2. Create settlement
        long amountMinor = Math.round(req.amount() * 100);
        Settlement settlement = Settlement.builder()
                .group(group)
                .fromUser(currentUser)
                .toUser(recipient)
                .amountMinor(amountMinor)
                .build();
        Settlement saved = settlementRepository.save(settlement);

        // 3. Log activity
        ActivityLog log = ActivityLog.builder()
                .group(group)
                .actor(currentUser)
                .action("settlement_created")
                .payload(Map.of(
                        "settlement_id", saved.getId(),
                        "to_user", req.toUser(),
                        "amount_minor", amountMinor
                ))
                .build();
        activityLogRepository.save(log);

        return SettlementResponse.from(saved);
    }
}