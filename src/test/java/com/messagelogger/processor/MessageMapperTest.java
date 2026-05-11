package com.messagelogger.processor;

import com.solacesystems.jcsmp.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageMapperTest {

    @Mock BytesXMLMessage msg;

    @Test
    void mapsBasicFields() throws Exception {
        Destination dest = mock(Destination.class);
        when(dest.getName()).thenReturn("rest/v1/orders");
        when(msg.getMessageId()).thenReturn("ID:abc-123");
        when(msg.getCorrelationId()).thenReturn("CORR-1");
        when(msg.getDestination()).thenReturn(dest);
        when(msg.getDeliveryMode()).thenReturn(DeliveryMode.PERSISTENT);
        when(msg.getRedelivered()).thenReturn(false);
        when(msg.isDMQEligible()).thenReturn(true);
        when(msg.getBytes()).thenReturn(new byte[]{'{', '}'});

        MessageRecord rec = MessageMapper.map(msg, "Q/test", false);

        assertEquals("ID:abc-123", rec.messageId);
        assertEquals("rest/v1/orders", rec.destinationTopic);
        assertEquals("PERSISTENT", rec.deliveryMode);
        assertEquals("false", rec.redelivered);
        assertEquals("true", rec.dmqEligible);
        assertEquals("Q/test", rec.queueName);
        assertNotNull(rec.timestamp);
        assertEquals(rec.timestamp, rec.receiveTimestamp);
    }

    @Test
    void base64EncodesNonJsonPayload() throws Exception {
        Destination dest = mock(Destination.class);
        when(dest.getName()).thenReturn("test/topic");
        when(msg.getDestination()).thenReturn(dest);
        when(msg.getDeliveryMode()).thenReturn(DeliveryMode.DIRECT);
        when(msg.getBytes()).thenReturn(new byte[]{0x01, 0x02, 0x03});

        MessageRecord rec = MessageMapper.map(msg, null, true);

        assertEquals("base64", rec.payloadEncoding);
        assertNotNull(rec.payload);
    }

    @Test
    void nullAckHandleInDirectMode() throws Exception {
        Destination dest = mock(Destination.class);
        when(dest.getName()).thenReturn("test/topic");
        when(msg.getDestination()).thenReturn(dest);
        when(msg.getDeliveryMode()).thenReturn(DeliveryMode.DIRECT);
        when(msg.getBytes()).thenReturn(new byte[0]);

        MessageRecord rec = MessageMapper.map(msg, null, true);

        assertNull(rec.ackHandle);
    }
}
