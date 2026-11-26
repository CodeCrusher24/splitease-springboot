package com.splitease.expense;

import com.splitease.expense.dto.ExpenseCreateRequest;
import com.splitease.expense.dto.ExpenseResponse;
import com.splitease.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @PathVariable Long groupId,
            @Valid @RequestBody ExpenseCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExpenseResponse response = expenseService.addExpense(groupId, req, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(expenseService.listExpenses(groupId, userDetails.getUser()));
    }
}