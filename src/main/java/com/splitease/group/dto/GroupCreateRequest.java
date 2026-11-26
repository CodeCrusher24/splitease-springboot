package com.splitease.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        @Size(max = 255) String description,
        @Size(min = 3, max = 3) String currency
) {
    public GroupCreateRequest {
        if (currency == null || currency.isBlank()) {
            currency = "INR";
        }
    }
}