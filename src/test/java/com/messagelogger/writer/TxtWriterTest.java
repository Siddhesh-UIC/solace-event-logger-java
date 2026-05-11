package com.messagelogger.writer;

import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TxtWriterTest {

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
        c.getDelimited().setDelimiter("|");
        return c;
    }

    private MessageRecord record() {
        return new MessageRecord(
            "2026-05-11T10:00:00.000Z", "2026-05-11T10:00:00.000Z",
            null, null, "ID:1", null, null, "rest/v1/orders", "Q",
            "PERSISTENT", null, "false", "true", null, "100",
            null, "POST", "/v1", null, "{}", "{}", "{}", null, null);
    }

    @Test
    void writesPipeDelimitedNoHeader() throws Exception {
        var fields = List.of(field("timestamp"), field("message_id"), field("destination_topic"));
        var w = new TxtWriter(cfg(), fields, ZoneId.of("UTC"));
        w.write(List.of(record()));
        w.close();
        List<String> lines = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow());
        assertEquals(1, lines.size());
        String[] parts = lines.get(0).split("\\|");
        assertEquals(3, parts.length);
        assertEquals("ID:1", parts[1]);
    }

    private CaptureSchema.FieldDefinition field(String name) {
        var f = new CaptureSchema.FieldDefinition(); f.setName(name); f.setEnabled(true); return f;
    }
}
