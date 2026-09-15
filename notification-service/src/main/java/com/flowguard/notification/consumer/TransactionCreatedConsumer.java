package com.flowguard.notification.consumer;

import com.flowguard.notification.event.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionCreatedConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(TransactionCreatedConsumer.class);

    @KafkaListener(
            topics = "transaction.created",
            groupId = "flowguard-notification")
    public void consume(TransactionCreatedEvent event) {
        logger.info(
                "Received transaction.created event: transactionId={}, amount={}, currency={}, merchantId={}",
                event.transactionId(),
                event.amount(),
                event.currency(),
                event.merchantId());
    }
}