package com.splitease.balance;

import com.splitease.common.ForbiddenException;
import com.splitease.expense.Expense;
import com.splitease.expense.ExpenseRepository;
import com.splitease.expense.ExpenseSplit;
import com.splitease.expense.ExpenseSplitRepository;
import com.splitease.group.GroupMemberRepository;
import com.splitease.settlement.Settlement;
import com.splitease.settlement.SettlementRepository;
import com.splitease.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final SettlementRepository settlementRepository;
    private final GroupMemberRepository groupMemberRepository;

    public BalanceService(
            ExpenseRepository expenseRepository,
            ExpenseSplitRepository expenseSplitRepository,
            SettlementRepository settlementRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.settlementRepository = settlementRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getBalances(Long groupId, User currentUser) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ForbiddenException("Not a member of this group");
        }

        List<Expense> expenses = expenseRepository.findActiveByGroupId(groupId);
        List<ExpenseSplit> splits = expenseSplitRepository.findAllByExpenseGroupId(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);

        return BalanceCalculator.compute(expenses, splits, settlements);
    }

    @Transactional(readOnly = true)
    public List<DebtSimplifier.Payment> getSimplifiedDebts(Long groupId, User currentUser) {
        Map<Long, Long> net = getBalances(groupId, currentUser);
        return DebtSimplifier.simplify(net);
    }
}