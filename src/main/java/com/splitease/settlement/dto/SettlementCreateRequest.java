package com.splitease.settlement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SettlementCreateRequest(
        @NotNull @JsonProperty("to_user") Long toUser,
        @NotNull @Positive Double amount
) {}