package com.splitease.expense;

import com.splitease.common.ForbiddenException;
import com.splitease.common.NotFoundException;
import com.splitease.expense.dto.*;
import com.splitease.group.Group;
import com.splitease.group.GroupMemberRepository;
import com.splitease.group.GroupRepository;
import com.splitease.user.User;
import com.splitease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseSplitRepository expenseSplitRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponse addExpense(Long groupId, ExpenseCreateRequest req, User currentUser) {
        // 1. Verify membership
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ForbiddenException("Not a member of this group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        // 2. Convert amount to minor units
        long amountMinor = Math.round(req.amount() * 100);

        // 3. Compute splits
        Map<Long, Long> shares = computeShares(amountMinor, req);

        // 4. Resolve all users in the split at once (avoid N+1)
        Set<Long> userIds = shares.keySet();
        Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        if (usersById.size() != userIds.size()) {
            throw new IllegalArgumentException("One or more users in the split do not exist");
        }

        // 5. Verify payer is a member of the group
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, req.paidBy())) {
            throw new IllegalArgumentException("Payer is not a member of this group");
        }

        User payer = userRepository.findById(req.paidBy())
                .orElseThrow(() -> new NotFoundException("Payer not found"));

        // 6. Create the expense
        Expense expense = Expense.builder()
                .group(group)
                .paidBy(payer)
                .description(req.description())
                .amountMinor(amountMinor)
                .currency("INR")
                .category(req.category())
                .splitType(req.splitType())
                .expenseDate(req.expenseDate())
                .build();
        Expense savedExpense = expenseRepository.save(expense);

        // 7. Create split rows
        List<ExpenseSplit> splitRows = new ArrayList<>();
        for (Map.Entry<Long, Long> e : shares.entrySet()) {
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(savedExpense)
                    .user(usersById.get(e.getKey()))
                    .shareMinor(e.getValue())
                    .build();
            splitRows.add(split);
        }
        expenseSplitRepository.saveAll(splitRows);

        // 8. Return response
        return ExpenseResponse.from(savedExpense, splitRows);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpenses(Long groupId, User currentUser) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ForbiddenException("Not a member of this group");
        }

        List<Expense> expenses = expenseRepository.findActiveByGroupId(groupId);

        return expenses.stream()
                .map(e -> ExpenseResponse.from(e, expenseSplitRepository.findByExpenseId(e.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Dispatch to the right splitter based on the split type.
     * @throws IllegalArgumentException if input is invalid.
     */
    private Map<Long, Long> computeShares(long amountMinor, ExpenseCreateRequest req) {
        String type = req.splitType();

        switch (type) {
            case "equal":
                List<Long> userIds = req.splits().stream().map(SplitInput::userId).toList();
                return ExpenseSplitter.splitEqual(amountMinor, userIds);

            case "exact":
                Map<Long, Long> exact = new LinkedHashMap<>();
                for (SplitInput s : req.splits()) {
                    exact.put(s.userId(), Math.round(s.value() * 100));
                }
                return ExpenseSplitter.splitExact(amountMinor, exact);

            case "percentage":
                Map<Long, Double> pct = new LinkedHashMap<>();
                for (SplitInput s : req.splits()) {
                    pct.put(s.userId(), s.value());
                }
                return ExpenseSplitter.splitPercentage(amountMinor, pct);

            case "shares":
                Map<Long, Integer> weights = new LinkedHashMap<>();
                for (SplitInput s : req.splits()) {
                    weights.put(s.userId(), s.value().intValue());
                }
                return ExpenseSplitter.splitShares(amountMinor, weights);

            default:
                throw new IllegalArgumentException("Unknown split type: " + type);
        }
    }
}