package com.messagelogger.writer;

import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;
import com.messagelogger.util.JsonUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonlWriterTest {

    @TempDir Path tempDir;

    private OutputConfig cfg() {
        OutputConfig c = new OutputConfig();
        c.setDirectory(tempDir.toString());
        c.setFilePattern("messages-{yyyyMMdd}.{format}");
        c.setEncoding("UTF-8");
        c.setMaxHistoryDays(30);
        c.getFlush().setEveryRecords(10000);
        c.getFlush().setEveryMs(60000);
        c.getFlush().setFsyncOnRotate(false);
        return c;
    }

    private List<CaptureSchema.FieldDefinition> fields(String... names) {
        return java.util.Arrays.stream(names).map(n -> {
            var f = new CaptureSchema.FieldDefinition();
            f.setName(n);
            f.setEnabled(true);
            return f;
        }).toList();
    }

    private MessageRecord record(String topic, String msgId) {
        return new MessageRecord(
            "2026-05-11T10:00:00.000Z", "2026-05-11T10:00:00.000Z",
            null, null, msgId, null, null, topic, "Q/test",
            "PERSISTENT", null, "false", "true", null, "100",
            "application/json", "POST", "/v1/orders", null,
            "{}", "{}", "{\"x\":1}", null, null);
    }

    @Test
    void writesSingleRecordAsJsonLine() throws Exception {
        var w = new JsonlWriter(cfg(), fields("timestamp", "message_id", "destination_topic"), ZoneId.of("UTC"));
        w.write(List.of(record("rest/v1/orders", "ID:1")));
        w.close();

        List<String> lines = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow());
        assertEquals(1, lines.size());
        var node = JsonUtil.readTree(lines.get(0));
        assertEquals("ID:1", node.get("message_id").asText());
        assertEquals("rest/v1/orders", node.get("destination_topic").asText());
        assertFalse(node.has("delivery_mode")); // not in enabled fields
    }

    @Test
    void writesBatchAsMultipleLines() throws Exception {
        var w = new JsonlWriter(cfg(), fields("timestamp", "message_id"), ZoneId.of("UTC"));
        w.write(List.of(record("a", "ID:1"), record("b", "ID:2"), record("c", "ID:3")));
        w.close();

        List<String> lines = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow());
        assertEquals(3, lines.size());
    }
}
