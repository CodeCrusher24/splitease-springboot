package com.splitease.balance.dto;

import java.util.Map;

public record BalancesResponse(Map<Long, Long> balances) {}