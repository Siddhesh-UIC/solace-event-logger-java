package com.messagelogger.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .disable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtil() {}

    public static String toCompactJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.debug("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    public static JsonNode readTree(byte[] raw) throws Exception {
        return MAPPER.readTree(raw);
    }

    public static String writeValueAsString(JsonNode node) throws Exception {
        return MAPPER.writeValueAsString(node);
    }
}
