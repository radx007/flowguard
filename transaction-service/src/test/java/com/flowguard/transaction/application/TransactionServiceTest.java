package com.flowguard.transaction.application;

import com.flowguard.transaction.domain.Transaction;
import com.flowguard.transaction.domain.TransactionStatus;
import com.flowguard.transaction.infrastructure.persistence.TransactionEntity;
import com.flowguard.transaction.infrastructure.persistence.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;

class TransactionServiceTest {

    private final TransactionRepository transactionRepository =
            mock(TransactionRepository.class);

    private final TransactionEventPublisher eventPublisher =
            mock(TransactionEventPublisher.class);

    private final TransactionService transactionService =
            new TransactionService(transactionRepository, eventPublisher);

    @Test
    void create_persistsAndReturnsTransaction() {
        when(transactionRepository.saveAndFlush(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.create(
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getIdempotencyKey()).isEqualTo("idem-123");
        assertThat(result.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(result.getCurrency()).isEqualTo("DZD");
        assertThat(result.getMerchantId()).isEqualTo("merchant-42");
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.PROCESSING);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(transactionRepository).saveAndFlush(any(TransactionEntity.class));
    }

    @Test
    void create_returnsExistingTransactionForSameIdempotencyKey() {
        UUID transactionId = UUID.randomUUID();

        TransactionEntity existing = new TransactionEntity(
                transactionId,
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42",
                TransactionStatus.PROCESSING,
                Instant.parse("2026-09-14T00:00:00Z"));

        when(transactionRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(java.util.Optional.of(existing));

        Transaction result = transactionService.create(
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42");

        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getAmount()).isEqualByComparingTo("5000.00");

        verify(transactionRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void create_rejectsSameIdempotencyKeyWithDifferentPayload() {
        TransactionEntity existing = new TransactionEntity(
                UUID.randomUUID(),
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42",
                TransactionStatus.PROCESSING,
                Instant.parse("2026-09-14T00:00:00Z"));

        when(transactionRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(java.util.Optional.of(existing));

        assertThatThrownBy(() -> transactionService.create(
                "idem-123",
                new BigDecimal("7000.00"),
                "DZD",
                "merchant-42"))
                .isInstanceOf(IdempotencyConflictException.class);

        verify(transactionRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void create_recoversFromConcurrentDuplicate() {
        UUID transactionId = UUID.randomUUID();

        TransactionEntity existing = new TransactionEntity(
                transactionId,
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42",
                TransactionStatus.PROCESSING,
                Instant.parse("2026-09-14T00:00:00Z"));

        when(transactionRepository.findByIdempotencyKey("idem-123"))
                .thenReturn(java.util.Optional.empty())
                .thenReturn(java.util.Optional.of(existing));

        when(transactionRepository.saveAndFlush(any(TransactionEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        Transaction result = transactionService.create(
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42");

        assertThat(result.getId()).isEqualTo(transactionId);
    }
}