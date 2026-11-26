package com.splitease.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SplitInput(
        @NotNull Long userId,
        @NotNull @PositiveOrZero Double value
) {}