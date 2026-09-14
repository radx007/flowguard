package com.flowguard.transaction.application;

import com.flowguard.transaction.domain.Transaction;
import com.flowguard.transaction.domain.TransactionStatus;
import com.flowguard.transaction.infrastructure.persistence.TransactionEntity;
import com.flowguard.transaction.infrastructure.persistence.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    private final TransactionRepository transactionRepository =
            mock(TransactionRepository.class);

    private final TransactionService transactionService =
            new TransactionService(transactionRepository);

    @Test
    void create_persistsAndReturnsTransaction() {
        when(transactionRepository.save(any(TransactionEntity.class)))
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

        verify(transactionRepository).save(any(TransactionEntity.class));
    }
}