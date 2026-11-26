package com.splitease.expense.dto;

import com.splitease.expense.Expense;
import com.splitease.expense.ExpenseSplit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExpenseResponse(
        Long id,
        Long groupId,
        Long paidBy,
        String description,
        Long amountMinor,
        String currency,
        String category,
        String splitType,
        LocalDate expenseDate,
        LocalDateTime createdAt,
        List<ExpenseSplitResponse> splits
) {
    public static ExpenseResponse from(Expense e, List<ExpenseSplit> splits) {
        return new ExpenseResponse(
                e.getId(),
                e.getGroup().getId(),
                e.getPaidBy().getId(),
                e.getDescription(),
                e.getAmountMinor(),
                e.getCurrency(),
                e.getCategory(),
                e.getSplitType(),
                e.getExpenseDate(),
                e.getCreatedAt(),
                splits.stream().map(ExpenseSplitResponse::from).toList()
        );
    }
}