package com.messagelogger.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper()
        .disable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtil() {}

    public static String toCompactJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
