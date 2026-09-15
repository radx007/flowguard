package com.flowguard.transaction.infrastructure.messaging;

import com.flowguard.transaction.application.TransactionEventPublisher;
import com.flowguard.transaction.event.TransactionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionEventPublisher implements TransactionEventPublisher {

    public static final String TRANSACTION_CREATED_TOPIC = "transaction.created";

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public KafkaTransactionEventPublisher(
            KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate.send(
                TRANSACTION_CREATED_TOPIC,
                event.transactionId().toString(),
                event);
    }
}