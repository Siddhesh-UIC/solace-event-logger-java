package com.messagelogger.writer;

import com.messagelogger.config.AppConfig;
import com.messagelogger.config.CaptureSchema;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

public class WriterFactory {
    public static MessageWriter create(AppConfig cfg,
                                       List<CaptureSchema.FieldDefinition> enabledFields) throws IOException {
        var output = cfg.getConsumer().getOutput();
        ZoneId zoneId = ZoneId.of(cfg.getService().getTimezone());
        return switch (output.getFormat()) {
            case "jsonl" -> new JsonlWriter(output, enabledFields, zoneId);
            case "csv"   -> new CsvWriter(output, enabledFields, zoneId);
            case "txt"   -> new TxtWriter(output, enabledFields, zoneId);
            case "log"   -> new LogPatternWriter(output, enabledFields, zoneId);
            default -> throw new IllegalArgumentException("Unknown format: " + output.getFormat());
        };
    }
}
