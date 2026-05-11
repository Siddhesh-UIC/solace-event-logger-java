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

class CsvWriterTest {

    @TempDir Path tempDir;

    private OutputConfig cfg(boolean includeHeader) {
        OutputConfig c = new OutputConfig();
        c.setDirectory(tempDir.toString());
        c.setFilePattern("messages-{yyyyMMdd}.{format}");
        c.setEncoding("UTF-8");
        c.setMaxHistoryDays(30);
        c.getFlush().setEveryRecords(10000);
        c.getFlush().setEveryMs(60000);
        c.getFlush().setFsyncOnRotate(false);
        c.getDelimited().setDelimiter("|");
        c.getDelimited().setQuoteChar("\"");
        c.getDelimited().setIncludeHeader(includeHeader);
        return c;
    }

    private List<CaptureSchema.FieldDefinition> fields(String... names) {
        return java.util.Arrays.stream(names).map(n -> {
            var f = new CaptureSchema.FieldDefinition(); f.setName(n); f.setEnabled(true); return f;
        }).toList();
    }

    private MessageRecord record() {
        return new MessageRecord(
            "2026-05-11T10:00:00.000Z", "2026-05-11T10:00:00.000Z",
            null, null, "ID:1", null, null, "rest/v1/orders", "Q/test",
            "PERSISTENT", null, "false", "true", null, "100",
            null, "POST", "/v1/orders", null, "{}", "{}", "{}", null, null);
    }

    @Test
    void writesHeaderOnOpen() throws Exception {
        var w = new CsvWriter(cfg(true), fields("timestamp", "message_id", "destination_topic"), ZoneId.of("UTC"));
        w.write(List.of(record()));
        w.close();
        List<String> lines = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow());
        assertEquals("timestamp|message_id|destination_topic", lines.get(0));
    }

    @Test
    void quotesFieldContainingDelimiter() throws Exception {
        var w = new CsvWriter(cfg(false), fields("message_id", "destination_topic"), ZoneId.of("UTC"));
        MessageRecord recWithPipe = new MessageRecord(
            "2026-05-11T10:00:00.000Z", "2026-05-11T10:00:00.000Z",
            null, null, "ID:pipe|test", null, null, "rest/v1", "Q",
            "PERSISTENT", null, "false", "true", null, "0",
            null, null, null, null, "{}", "{}", "{}", null, null);
        w.write(List.of(recWithPipe));
        w.close();
        List<String> lines = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow());
        assertTrue(lines.get(0).contains("\"ID:pipe|test\""));
    }

    @Test
    void noHeaderWhenDisabled() throws Exception {
        var w = new CsvWriter(cfg(false), fields("timestamp", "message_id"), ZoneId.of("UTC"));
        w.write(List.of(record()));
        w.close();
        List<String> lines = Files.readAllLines(Files.list(tempDir).findFirst().orElseThrow());
        assertEquals(1, lines.size());
        assertFalse(lines.get(0).startsWith("timestamp"));
    }
}
