package com.messagelogger.pipeline;

import com.messagelogger.processor.MessageRecord;
import com.messagelogger.writer.MessageWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessagePipelineTest {

    private MessageRecord rec(String id) {
        return new MessageRecord(
            "ts", "ts", null, null, id, null, null, "topic", null,
            "DIRECT", null, "false", "false", null, "0",
            null, null, null, null, "{}", "{}", "{}", null, null);
    }

    @Test
    void recordsFlowToWriter() throws Exception {
        List<MessageRecord> written = new ArrayList<>();
        MessageWriter writer = batch -> written.addAll(batch);

        MessagePipeline pipeline = new MessagePipeline(100, 2, 500, 100, writer);
        pipeline.start();

        pipeline.submit(rec("ID:1"));
        pipeline.submit(rec("ID:2"));

        pipeline.shutdown();

        assertEquals(2, written.size());
        assertTrue(written.stream().anyMatch(r -> "ID:1".equals(r.messageId)));
    }

    @Test
    void ackCalledAfterWrite() throws Exception {
        var ackCalled = new boolean[]{false};
        com.solacesystems.jcsmp.BytesXMLMessage msg =
            mock(com.solacesystems.jcsmp.BytesXMLMessage.class);
        doAnswer(inv -> { ackCalled[0] = true; return null; }).when(msg).ackMessage();

        MessageWriter writer = batch -> {};
        MessagePipeline pipeline = new MessagePipeline(100, 1, 500, 100, writer);
        pipeline.start();

        com.messagelogger.processor.AckHandle handle =
            new com.messagelogger.processor.AckHandle(msg);
        MessageRecord recWithAck = new MessageRecord(
            "ts", "ts", null, null, "ID:ack", null, null, "t", null,
            "PERSISTENT", null, "false", "false", null, "0",
            null, null, null, null, "{}", "{}", "{}", null, handle);

        pipeline.submit(recWithAck);
        pipeline.shutdown();

        assertTrue(ackCalled[0], "ack() should be called after write");
    }
}
