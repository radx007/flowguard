package com.flowguard.transaction.application;

import com.flowguard.transaction.infrastructure.persistence.TransactionEntity;
import com.flowguard.transaction.infrastructure.persistence.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
    }

    @Test
    void concurrentRequestsWithSameIdempotencyKey_createOnlyOneTransaction()
            throws Exception {

        String idempotencyKey = "concurrent-test";
        BigDecimal amount = new BigDecimal("5000.00");

        Callable<Void> request = () -> {
                    transactionService.create(
                            idempotencyKey,
                            amount,
                            "DZD",
                            "merchant-42");
            return null;
        };

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            List<Callable<Void>> requests = List.of(request, request);

            List<Future<Void>> results = executor.invokeAll(requests);

            for (Future<Void> result : results) {
                result.get();
            }
        }

        List<TransactionEntity> transactions =
                transactionRepository.findAll();

        assertThat(transactions)
                .hasSize(1)
                .first()
                .satisfies(transaction -> {
                    assertThat(transaction.getIdempotencyKey())
                            .isEqualTo(idempotencyKey);
                    assertThat(transaction.getAmount())
                            .isEqualByComparingTo(amount);
                    assertThat(transaction.getCurrency())
                            .isEqualTo("DZD");
                    assertThat(transaction.getMerchantId())
                            .isEqualTo("merchant-42");
                });
    }
}