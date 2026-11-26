package com.splitease.expense;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseSplitterTest {

    @Test
    void splitEqual_100_splits_34_33_33() {
        Map<Long, Long> shares = ExpenseSplitter.splitEqual(10000, List.of(1L, 2L, 3L));

        assertEquals(3, shares.size());
        assertEquals(10000L, shares.values().stream().mapToLong(Long::longValue).sum());
        // User 1 gets the extra paisa (first by ID)
        assertEquals(3334L, shares.get(1L));
        assertEquals(3333L, shares.get(2L));
        assertEquals(3333L, shares.get(3L));
    }

    @Test
    void splitEqual_emptyList_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExpenseSplitter.splitEqual(10000, List.of()));
    }

    @Test
    void splitExact_sumsToTotal() {
        Map<Long, Long> shares = ExpenseSplitter.splitExact(10000, Map.of(1L, 4000L, 2L, 3000L, 3L, 3000L));

        assertEquals(10000L, shares.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void splitExact_wrongSum_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExpenseSplitter.splitExact(10000, Map.of(1L, 4000L, 2L, 3000L, 3L, 2000L)));
    }

    @Test
    void splitPercentage_40_30_30() {
        Map<Long, Long> shares = ExpenseSplitter.splitPercentage(10000,
                Map.of(1L, 40.0, 2L, 30.0, 3L, 30.0));

        assertEquals(10000L, shares.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(4000L, shares.get(1L));
        assertEquals(3000L, shares.get(2L));
        assertEquals(3000L, shares.get(3L));
    }

    @Test
    void splitPercentage_withRemainder() {
        // 10000 * 33.33% = 3333, * 33.33% = 3333, * 33.34% = 3334
        Map<Long, Long> shares = ExpenseSplitter.splitPercentage(10000,
                Map.of(1L, 33.33, 2L, 33.33, 3L, 33.34));

        assertEquals(10000L, shares.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(3334L, shares.get(3L));
    }

    @Test
    void splitPercentage_not100_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExpenseSplitter.splitPercentage(10000,
                        Map.of(1L, 40.0, 2L, 30.0, 3L, 20.0)));
    }

    @Test
    void splitShares_2_1_1() {
        // 10000 split with weights 2:1:1 → 5000, 2500, 2500
        Map<Long, Long> shares = ExpenseSplitter.splitShares(10000,
                Map.of(1L, 2, 2L, 1, 3L, 1));

        assertEquals(10000L, shares.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(5000L, shares.get(1L));
        assertEquals(2500L, shares.get(2L));
        assertEquals(2500L, shares.get(3L));
    }
}