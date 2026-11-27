package com.splitease.balance.dto;

import java.util.List;

public record SimplifiedPaymentsResponse(List<PaymentDto> payments) {

    public record PaymentDto(Long fromUser, Long toUser, Long amountMinor) {}
}