package com.messagelogger.config;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class CaptureSchemaLoaderTest {

    private Path resource(String name) {
        return Path.of("src/test/resources/" + name);
    }

    @Test
    void loadsValidSchema() throws Exception {
        CaptureSchema schema = CaptureSchemaLoader.load(resource("test-schema-valid.json"));
        assertEquals(1, schema.getSchemaVersion());
        assertEquals(3, schema.enabledFields().size());
        assertEquals("timestamp", schema.enabledFields().get(0).getName());
    }

    @Test
    void disabledFieldExcludedFromEnabledList() throws Exception {
        CaptureSchema schema = CaptureSchemaLoader.load(resource("test-schema-valid.json"));
        assertTrue(schema.enabledFields().stream().noneMatch(f -> f.getName().equals("payload")));
    }

    @Test
    void rejectsUnsupportedSchemaVersion() {
        assertThrows(IllegalArgumentException.class,
            () -> CaptureSchemaLoader.load(resource("test-schema-unknown-version.json")));
    }

    @Test
    void warnsOnUnknownFieldNameButDoesNotFail() throws Exception {
        assertDoesNotThrow(
            () -> CaptureSchemaLoader.load(resource("test-schema-unknown-field.json")));
    }
}
