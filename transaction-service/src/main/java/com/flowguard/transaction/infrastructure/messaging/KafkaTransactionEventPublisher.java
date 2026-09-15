package com.flowguard.transaction.infrastructure.messaging;

import com.flowguard.transaction.application.TransactionEventPublisher;
import com.flowguard.transaction.event.TransactionCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionEventPublisher implements TransactionEventPublisher {

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;
    private final String topic;

    public KafkaTransactionEventPublisher(
            KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate,
            @Value("${flowguard.kafka.topics.transaction-created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate.send(
                topic,
                event.transactionId().toString(),
                event);
    }
}