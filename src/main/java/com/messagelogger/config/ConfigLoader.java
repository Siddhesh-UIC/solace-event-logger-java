package com.messagelogger.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    public static AppConfig load(Path configPath, Map<String, String> env) throws Exception {
        // 1. Read raw YAML text
        String rawYaml = Files.readString(configPath);

        // 2. Parse to flat property map for self-reference resolution
        Yaml yaml = new Yaml();
        Map<String, Object> rawMap = yaml.load(rawYaml);
        Map<String, String> flatYaml = flatten(rawMap, "");

        // 3. Resolve all ${VAR:-default} placeholders in the raw YAML text
        EnvVarResolver resolver = new EnvVarResolver(env, flatYaml);
        String resolvedYaml = resolver.resolve(rawYaml);

        // 4. Parse resolved YAML into AppConfig via Jackson
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AppConfig cfg = mapper.readValue(resolvedYaml, AppConfig.class);

        // 5. Apply Logback system property bridge before any logger is acquired
        applyLogbackBridge(cfg);

        return cfg;
    }

    private static void applyLogbackBridge(AppConfig cfg) {
        LoggingConfig.ServiceLogConfig svc = cfg.getLogging().getService();
        if (svc == null) return;
        setIfNotNull("LOG_LEVEL",       svc.getLevel());
        setIfNotNull("LOG_DIR",         svc.getDirectory());
        setIfNotNull("LOG_MAX_HISTORY", svc.getMaxHistoryDays() > 0 ? String.valueOf(svc.getMaxHistoryDays()) : null);

        // Translate YAML pattern placeholders to Logback pattern syntax
        if (svc.getPattern() != null) {
            String pattern = svc.getPattern()
                .replace("{timestamp}", "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX}")
                .replace("{level}",     "%-5level")
                .replace("{thread}",    "%thread")
                .replace("{logger}",    "%logger{30}")
                .replace("{message}",   "%msg");
            System.setProperty("LOG_PATTERN", pattern + "%n");
        }

        // Logback freezes config at first logger acquisition (class-load time, before main() runs).
        // Reset the context so the system properties set above are actually picked up.
        try {
            ch.qos.logback.classic.LoggerContext context =
                (ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
            context.reset();
            new ch.qos.logback.classic.util.ContextInitializer(context).autoConfig();
        } catch (Exception e) {
            // continue — Logback defaults remain if reset fails
        }
    }

    private static void setIfNotNull(String key, String value) {
        if (value != null) System.setProperty(key, value);
    }

    @SuppressWarnings("unchecked")
    static Map<String, String> flatten(Map<String, Object> map, String prefix) {
        Map<String, String> result = new HashMap<>();
        if (map == null) return result;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                result.putAll(flatten((Map<String, Object>) entry.getValue(), key));
            } else if (entry.getValue() != null) {
                result.put(key, entry.getValue().toString());
            }
        }
        return result;
    }
}
