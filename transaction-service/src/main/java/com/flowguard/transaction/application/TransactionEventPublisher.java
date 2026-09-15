package com.flowguard.transaction.application;

import com.flowguard.transaction.event.TransactionCreatedEvent;

public interface TransactionEventPublisher {

    void publish(TransactionCreatedEvent event);
}