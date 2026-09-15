package com.flowguard.ledger.consumer;

import com.flowguard.ledger.event.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCreatedConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(TransactionCreatedConsumer.class);

    @KafkaListener(
            topics = "${flowguard.kafka.topics.transaction-created}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(TransactionCreatedEvent event) {
        logger.info(
                "Received transaction.created event: transactionId={}, amount={}, currency={}, merchantId={}",
                event.transactionId(),
                event.amount(),
                event.currency(),
                event.merchantId());
    }
}