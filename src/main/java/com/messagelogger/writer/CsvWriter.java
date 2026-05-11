package com.messagelogger.writer;

import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class CsvWriter extends AbstractFileWriter {
    private final String delimiter;
    private final boolean includeHeader;
    public CsvWriter(OutputConfig cfg, List<CaptureSchema.FieldDefinition> enabledFields,
                     ZoneId zoneId) throws IOException {
        super(cfg, enabledFields, zoneId, "csv");
        this.delimiter     = cfg.getDelimited().getDelimiter();
        this.includeHeader = cfg.getDelimited().isIncludeHeader();
        // Write initial header now that subclass fields are initialized.
        if (includeHeader) {
            writeLine(enabledFields.stream()
                .map(CaptureSchema.FieldDefinition::getName)
                .collect(Collectors.joining(delimiter)));
        }
    }

    @Override
    protected void onFileOpened() throws IOException {
        // Called during super() (delimiter==null) and on each rotation (delimiter set).
        // Skip the first call; on rotation write header to the new file.
        if (delimiter == null) return;
        if (includeHeader) {
            writeLine(enabledFields.stream()
                .map(CaptureSchema.FieldDefinition::getName)
                .collect(Collectors.joining(delimiter)));
        }
    }

    @Override
    public synchronized void write(List<MessageRecord> batch) throws IOException {
        for (MessageRecord rec : batch) {
            writeLine(enabledFields.stream()
                .map(f -> quote(rec.getValue(f.getName())))
                .collect(Collectors.joining(delimiter)));
        }
    }

    private String quote(String value) {
        if (value == null) return "";
        if (value.contains(delimiter) || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
