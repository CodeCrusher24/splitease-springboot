package com.splitease.expense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record ExpenseCreateRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull @Positive Double amount,
        @NotNull @JsonProperty("paid_by") Long paidBy,
        @Size(max = 50) String category,
        @NotBlank @Pattern(regexp = "^(equal|exact|percentage|shares)$") String splitType,
        @NotEmpty @Valid List<SplitInput> splits,
        @NotNull @JsonProperty("expense_date") LocalDate expenseDate
) {
    public ExpenseCreateRequest {
        if (category == null || category.isBlank()) {
            category = "general";
        }
    }
}