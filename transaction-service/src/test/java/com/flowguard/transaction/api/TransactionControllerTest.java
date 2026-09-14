package com.flowguard.transaction.api;

import com.flowguard.transaction.application.TransactionService;
import com.flowguard.transaction.domain.Transaction;
import com.flowguard.transaction.domain.TransactionStatus;
import com.flowguard.transaction.application.IdempotencyConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void create_returnsCreatedTransaction() throws Exception {
        UUID transactionId = UUID.randomUUID();

        Transaction transaction = new Transaction(
                transactionId,
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42",
                TransactionStatus.PROCESSING,
                Instant.parse("2026-09-14T00:00:00Z"));

        when(transactionService.create(
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42"))
                .thenReturn(transaction);

        mockMvc.perform(post("/transactions")
                        .header("Idempotency-Key", "idem-123")
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": 5000.00,
                                  "currency": "DZD",
                                  "merchantId": "merchant-42"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.currency").value("DZD"))
                .andExpect(jsonPath("$.merchantId").value("merchant-42"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void create_rejectsInvalidAmount() throws Exception {
        mockMvc.perform(post("/transactions")
                        .header("Idempotency-Key", "idem-123")
                        .contentType("application/json")
                        .content("""
                                {
                                  "amount": 0,
                                  "currency": "DZD",
                                  "merchantId": "merchant-42"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returnsConflictForIdempotencyConflict() throws Exception {
        when(transactionService.create(
                "idem-123",
                new BigDecimal("5000.00"),
                "DZD",
                "merchant-42"))
                .thenThrow(new IdempotencyConflictException(
                        "Idempotency key has already been used with a different request."));

        mockMvc.perform(post("/transactions")
                        .header("Idempotency-Key", "idem-123")
                        .contentType("application/json")
                        .content("""
                            {
                              "amount": 5000.00,
                              "currency": "DZD",
                              "merchantId": "merchant-42"
                            }
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));
    }
}