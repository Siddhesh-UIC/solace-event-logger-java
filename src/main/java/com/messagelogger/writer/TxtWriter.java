package com.messagelogger.writer;

import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class TxtWriter extends AbstractFileWriter {
    private final String delimiter;

    public TxtWriter(OutputConfig cfg, List<CaptureSchema.FieldDefinition> enabledFields,
                     ZoneId zoneId) throws IOException {
        super(cfg, enabledFields, zoneId, "txt");
        this.delimiter = cfg.getDelimited().getDelimiter();
    }

    @Override
    public synchronized void write(List<MessageRecord> batch) throws IOException {
        for (MessageRecord rec : batch) {
            writeLine(enabledFields.stream()
                .map(f -> nullSafe(rec.getValue(f.getName())))
                .collect(Collectors.joining(delimiter)));
        }
    }

    private String nullSafe(String v) { return v == null ? "" : v; }
}
