package com.flowguard.transaction.api;

import com.flowguard.transaction.domain.Transaction;
import com.flowguard.transaction.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        BigDecimal amount,
        String currency,
        String merchantId,
        TransactionStatus status,
        Instant createdAt
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantId(),
                transaction.getStatus(),
                transaction.getCreatedAt());
    }
}