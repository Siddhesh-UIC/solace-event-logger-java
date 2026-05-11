package com.messagelogger.writer;

import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LogPatternWriterTest {

    @TempDir Path tempDir;

    private OutputConfig cfg(String pattern) {
        OutputConfig c = new OutputConfig();
        c.setDirectory(tempDir.toString());
        c.setFilePattern("messages-{yyyyMMdd}.{format}");
        c.setEncoding("UTF-8");
        c.setMaxHistoryDays(30);
        c.getFlush().setEveryRecords(10000);
        c.getFlush().setEveryMs(60000);
        c.getFlush().setFsyncOnRotate(false);
        c.getLogFormat().setPattern(pattern);
        return c;
    }

    private List<CaptureSchema.FieldDefinition> enabled(String... names) {
        return Arrays.stream(names).map(n -> {
            var f = new CaptureSchema.FieldDefinition(); f.setName(n); f.setEnabled(true); return f;
        }).toList();
    }

    private MessageRecord record() {
        return new MessageRecord(
            "2026-05-11T10:00:00.000Z", "2026-05-11T10:00:00.000Z",
            null, null, "ID:1", null, null, "rest/v1/orders", "Q",
            "PERSISTENT", null, "false", "true", null, "100",
            null, "POST", "/v1/orders", null, "{}", "{}", "{}", null, null);
    }

    @Test
    void substitutesPlaceholders() throws Exception {
        var w = new LogPatternWriter(
            cfg("{timestamp} topic={destination_topic}"),
            enabled("timestamp", "destination_topic"), ZoneId.of("UTC"));
        w.write(List.of(record()));
        w.close();
        String line = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow()).get(0);
        assertTrue(line.contains("2026-05-11T10:00:00.000Z"));
        assertTrue(line.contains("topic=rest/v1/orders"));
    }

    @Test
    void dropsTokenForDisabledField() throws Exception {
        // delivery_mode is NOT in enabledFields
        var w = new LogPatternWriter(
            cfg("{timestamp} mode={delivery_mode} topic={destination_topic}"),
            enabled("timestamp", "destination_topic"), ZoneId.of("UTC"));
        w.write(List.of(record()));
        w.close();
        String line = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow()).get(0);
        assertFalse(line.contains("mode="), "Disabled field token should be dropped");
        assertTrue(line.contains("topic=rest/v1/orders"));
    }
}
