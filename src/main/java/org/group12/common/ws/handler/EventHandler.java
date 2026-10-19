package org.group12.common.ws.handler;


import org.group12.common.ws.event.DomainEvent;

public interface EventHandler<T extends DomainEvent> {

    Class<T> support();

    void handle(T event);
}