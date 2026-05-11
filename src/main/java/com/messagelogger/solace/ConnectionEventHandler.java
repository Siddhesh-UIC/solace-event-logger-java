package com.messagelogger.solace;

import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionEventHandler implements SessionEventHandler {
    private static final Logger log = LoggerFactory.getLogger(ConnectionEventHandler.class);

    @Override
    public void handleEvent(SessionEventArgs event) {
        switch (event.getEvent()) {
            case DOWN_ERROR    -> log.error("Session DOWN info={}", event.getInfo());
            case RECONNECTING  -> log.warn("Session RECONNECTING");
            case RECONNECTED   -> log.info("Session RECONNECTED");
            default            -> log.debug("Session event: {}", event.getEvent());
        }
    }
}
