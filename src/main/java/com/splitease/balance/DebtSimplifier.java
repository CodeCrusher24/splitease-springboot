package com.splitease.balance;

import java.util.*;

public final class DebtSimplifier {

    private DebtSimplifier() {}

    public record Payment(Long fromUser, Long toUser, Long amountMinor) {}

    /**
     * Given net balances, return the minimum number of payments
     * (at most n-1) to zero everyone out.
     */
    public static List<Payment> simplify(Map<Long, Long> net) {
        // Max-heap for creditors (positive net). Use negated amount as comparator key.
        PriorityQueue<long[]> creditors = new PriorityQueue<>(
                (a, b) -> Long.compare(b[1], a[1])  // descending by amount
        );
        // Min-heap for debtors (negative net). Most-negative first.
        PriorityQueue<long[]> debtors = new PriorityQueue<>(
                Comparator.comparingLong(a -> a[1])  // ascending by (negative) amount
        );

        for (Map.Entry<Long, Long> e : net.entrySet()) {
            if (e.getValue() > 0) {
                creditors.add(new long[]{e.getKey(), e.getValue()});
            } else if (e.getValue() < 0) {
                debtors.add(new long[]{e.getKey(), e.getValue()});
            }
        }

        List<Payment> payments = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            long[] creditor = creditors.poll();
            long[] debtor = debtors.poll();

            long creditorAmount = creditor[1];
            long debtorAmount = -debtor[1];  // make positive
            long paid = Math.min(creditorAmount, debtorAmount);

            payments.add(new Payment(debtor[0], creditor[0], paid));

            if (creditorAmount > paid) {
                creditors.add(new long[]{creditor[0], creditorAmount - paid});
            }
            if (debtorAmount > paid) {
                debtors.add(new long[]{debtor[0], -(debtorAmount - paid)});
            }
        }

        return payments;
    }
}