package com.flowguard.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final String idempotencyKey;
    private final BigDecimal amount;
    private final String currency;
    private final String merchantId;
    private final TransactionStatus status;
    private final Instant createdAt;

    public Transaction(
            UUID id,
            String idempotencyKey,
            BigDecimal amount,
            String currency,
            String merchantId,
            TransactionStatus status,
            Instant createdAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = currency;
        this.merchantId = merchantId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}