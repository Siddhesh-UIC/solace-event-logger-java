package com.messagelogger.config;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @Test
    void loadsMinimalConfig() throws Exception {
        AppConfig cfg = ConfigLoader.load(
            Path.of("src/test/resources/test-config-minimal.yaml"), Map.of());
        assertEquals("test-logger", cfg.getService().getName());
        assertEquals("GUARANTEED", cfg.getConsumer().getMode());
    }

    @Test
    void resolvesEnvVarWithDefault() throws Exception {
        AppConfig cfg = ConfigLoader.load(
            Path.of("src/test/resources/test-config-with-vars.yaml"),
            Map.of("SOLACE_PASSWORD", "secret"));
        assertEquals("tcp://localhost:55555", cfg.getSolace().getHost());
        assertEquals("secret", cfg.getSolace().getPassword());
    }

    @Test
    void resolvesYamlSelfReference() throws Exception {
        AppConfig cfg = ConfigLoader.load(
            Path.of("src/test/resources/test-config-with-vars.yaml"),
            Map.of("SOLACE_PASSWORD", "x"));
        assertEquals("svc-inst-1", cfg.getSolace().getClientName());
    }

    @Test
    void failsWhenRequiredEnvVarMissing() {
        assertThrows(IllegalStateException.class, () ->
            ConfigLoader.load(Path.of("src/test/resources/test-config-with-vars.yaml"),
                              Map.of())); // SOLACE_PASSWORD missing
    }
}
