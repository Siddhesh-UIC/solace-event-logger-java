package com.messagelogger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

public class ConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);
    private static final Set<String> VALID_MODES = Set.of("GUARANTEED", "DIRECT");
    private static final Set<String> VALID_FORMATS = Set.of("log", "txt", "csv", "jsonl");

    public static void validate(AppConfig cfg) {
        String password = cfg.getSolace().getPassword();
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("SOLACE_PASSWORD is required but not set");
        }

        String mode = cfg.getConsumer().getMode();
        if (!VALID_MODES.contains(mode)) {
            throw new IllegalStateException(
                "consumer.mode must be GUARANTEED or DIRECT, got: " + mode);
        }

        String format = cfg.getConsumer().getOutput().getFormat();
        if (!VALID_FORMATS.contains(format)) {
            throw new IllegalStateException(
                "consumer.output.format must be one of log|txt|csv|jsonl, got: " + format);
        }

        // Warn if unused mode block has non-default values
        if ("DIRECT".equals(mode)) {
            ConsumerConfig.GuaranteedConfig g = cfg.getConsumer().getGuaranteed();
            if (g != null && g.getQueue() != null) {
                String qName = g.getQueue().getName();
                if (qName != null && !qName.equals("Q/logger")) {
                    log.warn("consumer.mode=DIRECT but guaranteed.queue.name='{}' — likely misconfigured", qName);
                }
            }
        }
    }
}
