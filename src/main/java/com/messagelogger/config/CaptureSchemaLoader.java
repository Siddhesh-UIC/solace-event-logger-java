package com.messagelogger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;

public class CaptureSchemaLoader {
    private static final Logger log = LoggerFactory.getLogger(CaptureSchemaLoader.class);
    private static final int SUPPORTED_VERSION = 1;
    private static final Set<String> KNOWN_FIELDS = Set.of(
        "timestamp", "receive_timestamp", "sender_timestamp", "broker_timestamp",
        "message_id", "correlation_id", "replication_group_message_id",
        "destination_topic", "queue_name", "delivery_mode", "priority",
        "redelivered", "dmq_eligible", "expiration", "payload_size_bytes",
        "content_type", "http_method", "http_uri", "http_status",
        "headers", "user_properties", "payload"
    );

    public static CaptureSchema load(Path path) throws Exception {
        ObjectMapper mapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CaptureSchema schema = mapper.readValue(path.toFile(), CaptureSchema.class);

        if (schema.getSchemaVersion() != SUPPORTED_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported capture-schema version: " + schema.getSchemaVersion() +
                " (expected " + SUPPORTED_VERSION + ")");
        }

        if (schema.getFields() != null) {
            for (CaptureSchema.FieldDefinition f : schema.getFields()) {
                if (!KNOWN_FIELDS.contains(f.getName())) {
                    log.warn("capture-schema: unknown field '{}' — will be ignored", f.getName());
                }
            }
        }

        log.info("Loaded capture schema version={} fields-enabled={}",
            schema.getSchemaVersion(), schema.enabledFields().size());
        return schema;
    }
}
