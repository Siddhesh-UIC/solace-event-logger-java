package com.messagelogger.writer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;
import com.messagelogger.util.JsonUtil;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

public class JsonlWriter extends AbstractFileWriter {

    public JsonlWriter(OutputConfig cfg, List<CaptureSchema.FieldDefinition> enabledFields,
                       ZoneId zoneId) throws IOException {
        super(cfg, enabledFields, zoneId, "jsonl");
    }

    @Override
    public synchronized void write(List<MessageRecord> batch) throws IOException {
        for (MessageRecord rec : batch) {
            ObjectNode node = JsonUtil.createObjectNode();
            for (CaptureSchema.FieldDefinition field : enabledFields) {
                String value = rec.getValue(field.getName());
                if (value == null) {
                    node.putNull(field.getName());
                } else if (value.startsWith("{") || value.startsWith("[")) {
                    try {
                        node.set(field.getName(), JsonUtil.readTree(value));
                        continue;
                    } catch (Exception ignored) {}
                    node.put(field.getName(), value);
                } else {
                    node.put(field.getName(), value);
                }
            }
            try {
                writeLine(JsonUtil.writeValueAsString(node));
            } catch (Exception e) {
                throw new IOException("Failed to serialize record", e);
            }
        }
    }
}
