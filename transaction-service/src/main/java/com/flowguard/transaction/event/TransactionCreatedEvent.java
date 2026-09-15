package com.flowguard.transaction.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        String eventType,
        int version,
        Instant occurredAt,
        UUID transactionId,
        BigDecimal amount,
        String currency,
        String merchantId) {

    public static final String EVENT_TYPE = "transaction.created";
    public static final int VERSION = 1;
}