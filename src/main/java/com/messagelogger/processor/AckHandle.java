package com.messagelogger.processor;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;

public class AckHandle {
    private final BytesXMLMessage message;

    public AckHandle(BytesXMLMessage message) {
        this.message = message;
    }

    public void ack() throws JCSMPException {
        message.ackMessage();
    }
}
