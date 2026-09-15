package com.flowguard.transaction.application;

import com.flowguard.transaction.domain.Transaction;
import com.flowguard.transaction.domain.TransactionStatus;
import com.flowguard.transaction.event.TransactionCreatedEvent;
import com.flowguard.transaction.infrastructure.persistence.TransactionEntity;
import com.flowguard.transaction.infrastructure.persistence.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionEventPublisher transactionEventPublisher) {
        this.transactionRepository = transactionRepository;
        this.eventPublisher = transactionEventPublisher;
    }

    public Transaction create(
            String idempotencyKey,
            BigDecimal amount,
            String currency,
            String merchantId) {

        return transactionRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> resolveExisting(
                        existing,
                        amount,
                        currency,
                        merchantId))
                .orElseGet(() -> createNew(
                        idempotencyKey,
                        amount,
                        currency,
                        merchantId));
    }

    private Transaction resolveExisting(
            TransactionEntity existing,
            BigDecimal amount,
            String currency,
            String merchantId) {

        if (!matches(existing, amount, currency, merchantId)) {
            throw new IdempotencyConflictException(
                    "Idempotency key has already been used with a different request.");
        }

        return toDomain(existing);
    }

    private Transaction createNew(
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

        try {
            transactionRepository.saveAndFlush(entity);

            eventPublisher.publish(new TransactionCreatedEvent(
                    UUID.randomUUID(),
                    TransactionCreatedEvent.EVENT_TYPE,
                    TransactionCreatedEvent.VERSION,
                    Instant.now(),
                    transaction.getId(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getMerchantId()));
            return transaction;
        } catch (DataIntegrityViolationException exception) {
            return transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .map(existing -> resolveExisting(
                            existing,
                            amount,
                            currency,
                            merchantId))
                    .orElseThrow(() -> exception);
        }
    }

    private boolean matches(
            TransactionEntity existing,
            BigDecimal amount,
            String currency,
            String merchantId) {

        return existing.getAmount().compareTo(amount) == 0
                && existing.getCurrency().equals(currency)
                && existing.getMerchantId().equals(merchantId);
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getMerchantId(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}