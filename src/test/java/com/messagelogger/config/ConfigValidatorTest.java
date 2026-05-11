package com.messagelogger.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigValidatorTest {

    private AppConfig validGuaranteedConfig() {
        AppConfig cfg = new AppConfig();
        cfg.getSolace().setPassword("secret");
        cfg.getConsumer().setMode("GUARANTEED");
        cfg.getConsumer().getOutput().setFormat("jsonl");
        return cfg;
    }

    @Test
    void validConfigPassesWithoutException() {
        assertDoesNotThrow(() -> ConfigValidator.validate(validGuaranteedConfig()));
    }

    @Test
    void missingPasswordFails() {
        AppConfig cfg = validGuaranteedConfig();
        cfg.getSolace().setPassword(null);
        assertThrows(IllegalStateException.class, () -> ConfigValidator.validate(cfg));
    }

    @Test
    void invalidModeFails() {
        AppConfig cfg = validGuaranteedConfig();
        cfg.getConsumer().setMode("UNKNOWN");
        assertThrows(IllegalStateException.class, () -> ConfigValidator.validate(cfg));
    }

    @Test
    void invalidFormatFails() {
        AppConfig cfg = validGuaranteedConfig();
        cfg.getConsumer().getOutput().setFormat("xml");
        assertThrows(IllegalStateException.class, () -> ConfigValidator.validate(cfg));
    }
}
