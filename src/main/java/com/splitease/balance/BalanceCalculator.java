package com.splitease.balance;

import com.splitease.expense.Expense;
import com.splitease.expense.ExpenseSplit;
import com.splitease.settlement.Settlement;

import java.util.*;

public final class BalanceCalculator {

    private BalanceCalculator() {}

    /**
     * Compute net balance for each user in a group.
     * Positive = owed money. Negative = owes money.
     * Sum of all balances is always zero.
     */
    public static Map<Long, Long> compute(
            List<Expense> expenses,
            List<ExpenseSplit> splits,
            List<Settlement> settlements
    ) {
        Map<Long, Long> net = new HashMap<>();

        // 1. What each user paid
        for (Expense e : expenses) {
            net.merge(e.getPaidBy().getId(), e.getAmountMinor(), Long::sum);
        }

        // 2. What each user owes (their share)
        for (ExpenseSplit s : splits) {
            net.merge(s.getUser().getId(), -s.getShareMinor(), Long::sum);
        }

        // 3. Settlements: payer's debt goes down, receiver's credit goes down
        for (Settlement st : settlements) {
            net.merge(st.getFromUser().getId(), st.getAmountMinor(), Long::sum);
            net.merge(st.getToUser().getId(), -st.getAmountMinor(), Long::sum);
        }

        // Invariant: nets must sum to zero
        long sum = net.values().stream().mapToLong(Long::longValue).sum();
        if (sum != 0) {
            throw new IllegalStateException("Net balances do not sum to zero: " + sum);
        }

        return net;
    }
}