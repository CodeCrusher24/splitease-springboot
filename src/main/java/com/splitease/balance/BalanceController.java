package com.splitease.balance;

import com.splitease.balance.dto.BalancesResponse;
import com.splitease.balance.dto.SimplifiedPaymentsResponse;
import com.splitease.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups/{groupId}")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/balances")
    public ResponseEntity<BalancesResponse> getBalances(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Map<Long, Long> balances = balanceService.getBalances(groupId, userDetails.getUser());
        return ResponseEntity.ok(new BalancesResponse(balances));
    }

    @GetMapping("/simplify")
    public ResponseEntity<SimplifiedPaymentsResponse> getSimplifiedDebts(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<DebtSimplifier.Payment> payments = balanceService.getSimplifiedDebts(groupId, userDetails.getUser());
        List<SimplifiedPaymentsResponse.PaymentDto> dtos = payments.stream()
                .map(p -> new SimplifiedPaymentsResponse.PaymentDto(p.fromUser(), p.toUser(), p.amountMinor()))
                .toList();
        return ResponseEntity.ok(new SimplifiedPaymentsResponse(dtos));
    }
}