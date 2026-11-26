package com.splitease.expense.dto;

import com.splitease.expense.ExpenseSplit;

public record ExpenseSplitResponse(Long userId, Long shareMinor) {
    public static ExpenseSplitResponse from(ExpenseSplit split) {
        return new ExpenseSplitResponse(split.getUser().getId(), split.getShareMinor());
    }
}