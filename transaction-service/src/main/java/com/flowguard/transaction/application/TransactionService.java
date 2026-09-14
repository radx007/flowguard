package com.flowguard.transaction.application;

import com.flowguard.transaction.domain.Transaction;
import com.flowguard.transaction.domain.TransactionStatus;
import com.flowguard.transaction.infrastructure.persistence.TransactionEntity;
import com.flowguard.transaction.infrastructure.persistence.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction create(
            String idempotencyKey,
            BigDecimal amount,
            String currency,
            String merchantId) {

        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                idempotencyKey,
                amount,
                currency,
                merchantId,
                TransactionStatus.PROCESSING,
                Instant.now());

        TransactionEntity entity = new TransactionEntity(
                transaction.getId(),
                transaction.getIdempotencyKey(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantId(),
                transaction.getStatus(),
                transaction.getCreatedAt());

        transactionRepository.save(entity);

        return transaction;
    }
}