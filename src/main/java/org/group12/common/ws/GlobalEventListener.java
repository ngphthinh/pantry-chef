package org.group12.common.ws;

import lombok.RequiredArgsConstructor;
import org.group12.common.ws.event.DomainEvent;
import org.group12.common.ws.handler.EventDispatcher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GlobalEventListener {

    private final EventDispatcher dispatcher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DomainEvent event) {
        dispatcher.dispatch(event);
    }

}