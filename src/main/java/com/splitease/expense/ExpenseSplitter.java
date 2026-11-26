package com.splitease.expense;

import java.util.*;

public final class ExpenseSplitter {

    private ExpenseSplitter() {
        // Utility class — no instances
    }

    /**
     * Split total equally among users. Remainder goes to the first N users
     * (deterministically, sorted by user ID).
     */
    public static Map<Long, Long> splitEqual(long totalMinor, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one user");
        }

        int n = userIds.size();
        long base = totalMinor / n;
        long remainder = totalMinor % n;

        List<Long> sorted = new ArrayList<>(userIds);
        Collections.sort(sorted);

        Map<Long, Long> shares = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            Long uid = sorted.get(i);
            shares.put(uid, base + (i < remainder ? 1 : 0));
        }

        long sum = shares.values().stream().mapToLong(Long::longValue).sum();
        if (sum != totalMinor) {
            throw new IllegalStateException("Split does not sum to total");
        }
        return shares;
    }

    /**
     * Exact split: verify that amounts sum to the total.
     */
    public static Map<Long, Long> splitExact(long totalMinor, Map<Long, Long> amounts) {
        long sum = amounts.values().stream().mapToLong(Long::longValue).sum();
        if (sum != totalMinor) {
            throw new IllegalArgumentException("Exact amounts must sum to the total expense amount");
        }
        return new LinkedHashMap<>(amounts);
    }

    /**
     * Percentage split using largest remainder method.
     */
    public static Map<Long, Long> splitPercentage(long totalMinor, Map<Long, Double> percentages) {
        double totalPct = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalPct - 100.0) > 0.01) {
            throw new IllegalArgumentException("Percentages must sum to 100");
        }

        // Raw (double) share per user
        Map<Long, Double> raw = new LinkedHashMap<>();
        for (Map.Entry<Long, Double> e : percentages.entrySet()) {
            raw.put(e.getKey(), (e.getValue() / 100.0) * totalMinor);
        }

        // Floor and track the fractional remainder
        Map<Long, Long> floored = new LinkedHashMap<>();
        long sumFloored = 0;
        for (Map.Entry<Long, Double> e : raw.entrySet()) {
            long f = (long) Math.floor(e.getValue());
            floored.put(e.getKey(), f);
            sumFloored += f;
        }

        long leftover = totalMinor - sumFloored;

        // Sort by fractional part descending, tie-break by user id
        List<Long> order = new ArrayList<>(raw.keySet());
        order.sort((a, b) -> {
            double fracA = raw.get(a) - Math.floor(raw.get(a));
            double fracB = raw.get(b) - Math.floor(raw.get(b));
            int cmp = Double.compare(fracB, fracA); // descending
            return cmp != 0 ? cmp : a.compareTo(b);
        });

        for (int i = 0; i < leftover && i < order.size(); i++) {
            floored.merge(order.get(i), 1L, Long::sum);
        }

        long sum = floored.values().stream().mapToLong(Long::longValue).sum();
        if (sum != totalMinor) {
            throw new IllegalStateException("Percentage split does not sum to total");
        }
        return floored;
    }

    /**
     * Weighted shares (e.g., 2 shares vs 1 share).
     */
    public static Map<Long, Long> splitShares(long totalMinor, Map<Long, Integer> shares) {
        long totalShares = shares.values().stream().mapToLong(Integer::longValue).sum();
        if (totalShares <= 0) {
            throw new IllegalArgumentException("Total shares must be greater than zero");
        }

        Map<Long, Double> raw = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> e : shares.entrySet()) {
            raw.put(e.getKey(), ((double) e.getValue() / totalShares) * totalMinor);
        }

        Map<Long, Long> floored = new LinkedHashMap<>();
        long sumFloored = 0;
        for (Map.Entry<Long, Double> e : raw.entrySet()) {
            long f = (long) Math.floor(e.getValue());
            floored.put(e.getKey(), f);
            sumFloored += f;
        }

        long leftover = totalMinor - sumFloored;

        List<Long> order = new ArrayList<>(raw.keySet());
        order.sort((a, b) -> {
            double fracA = raw.get(a) - Math.floor(raw.get(a));
            double fracB = raw.get(b) - Math.floor(raw.get(b));
            int cmp = Double.compare(fracB, fracA);
            return cmp != 0 ? cmp : a.compareTo(b);
        });

        for (int i = 0; i < leftover && i < order.size(); i++) {
            floored.merge(order.get(i), 1L, Long::sum);
        }

        long sum = floored.values().stream().mapToLong(Long::longValue).sum();
        if (sum != totalMinor) {
            throw new IllegalStateException("Shares split does not sum to total");
        }
        return floored;
    }
}