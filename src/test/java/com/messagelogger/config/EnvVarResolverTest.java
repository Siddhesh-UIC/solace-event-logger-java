package com.messagelogger.config;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EnvVarResolverTest {

    @Test
    void resolvesEnvVarPresent() {
        EnvVarResolver r = new EnvVarResolver(Map.of("MY_VAR", "hello"), Map.of());
        assertEquals("hello", r.resolve("${MY_VAR}"));
    }

    @Test
    void usesDefaultWhenEnvAbsent() {
        EnvVarResolver r = new EnvVarResolver(Map.of(), Map.of());
        assertEquals("fallback", r.resolve("${MISSING:-fallback}"));
    }

    @Test
    void resolvesYamlPropertyReference() {
        EnvVarResolver r = new EnvVarResolver(Map.of(), Map.of("service.name", "my-svc"));
        assertEquals("my-svc", r.resolve("${service.name}"));
    }

    @Test
    void envVarTakesPriorityOverYamlProperty() {
        EnvVarResolver r = new EnvVarResolver(Map.of("service.name", "from-env"),
                                               Map.of("service.name", "from-yaml"));
        assertEquals("from-env", r.resolve("${service.name}"));
    }

    @Test
    void throwsWhenRequiredVarMissing() {
        EnvVarResolver r = new EnvVarResolver(Map.of(), Map.of());
        assertThrows(IllegalStateException.class, () -> r.resolve("${REQUIRED_VAR}"));
    }

    @Test
    void resolvesMultiplePlaceholdersInString() {
        EnvVarResolver r = new EnvVarResolver(Map.of("A", "foo"), Map.of("b", "bar"));
        assertEquals("foo-bar", r.resolve("${A}-${b}"));
    }

    @Test
    void literalStringPassesThrough() {
        EnvVarResolver r = new EnvVarResolver(Map.of(), Map.of());
        assertEquals("plain-text", r.resolve("plain-text"));
    }
}
