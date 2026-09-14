package com.flowguard.transaction.api;

import com.flowguard.transaction.application.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid CreateTransactionRequest request) {

        var transaction = transactionService.create(
                idempotencyKey,
                request.amount(),
                request.currency(),
                request.merchantId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transaction));
    }
}