package com.splitease.user;

import com.splitease.expense.ExpenseRepository;
import com.splitease.group.GroupMemberRepository;
import com.splitease.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ExpenseRepository expenseRepository;
    private final GroupMemberRepository groupMemberRepository;

    public UserController(ExpenseRepository expenseRepository, GroupMemberRepository groupMemberRepository) {
        this.expenseRepository = expenseRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @GetMapping("/me/stats")
    public ResponseEntity<Map<String, Object>> getMyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();

        long totalGroups = groupMemberRepository.countByUserId(userId);
        long totalPaid = expenseRepository.sumAmountByPaidByAndNotDeleted(userId);

        // Pending settlements requires the balance calculation.
        // For now, return 0 — we'll wire this properly after verifying the basics work.
        long pendingSettlements = 0L;

        return ResponseEntity.ok(Map.of(
                "total_groups", totalGroups,
                "total_expenses_minor", totalPaid,
                "pending_settlements_minor", pendingSettlements
        ));
    }
}