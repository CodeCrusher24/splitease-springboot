package com.splitease.settlement;

import com.splitease.security.CustomUserDetails;
import com.splitease.settlement.dto.SettlementCreateRequest;
import com.splitease.settlement.dto.SettlementResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> create(
            @PathVariable Long groupId,
            @Valid @RequestBody SettlementCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SettlementResponse response = settlementService.createSettlement(groupId, req, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}