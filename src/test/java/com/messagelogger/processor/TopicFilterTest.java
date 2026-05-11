package com.messagelogger.processor;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TopicFilterTest {

    @Test
    void acceptsWhenIncludeIsWildcard() {
        TopicFilter f = new TopicFilter(List.of(">"), List.of());
        assertTrue(f.accept("rest/v1/orders"));
    }

    @Test
    void rejectsWhenNotMatchingInclude() {
        TopicFilter f = new TopicFilter(List.of("rest/v1/>"), List.of());
        assertFalse(f.accept("rest/v2/orders"));
    }

    @Test
    void excludeTakesPriority() {
        TopicFilter f = new TopicFilter(List.of(">"), List.of("*/heartbeat/>"));
        assertFalse(f.accept("svc/heartbeat/ping"));
    }

    @Test
    void singleLevelWildcardMatchesOneSegment() {
        TopicFilter f = new TopicFilter(List.of("rest/*/orders"), List.of());
        assertTrue(f.accept("rest/v1/orders"));
        assertFalse(f.accept("rest/v1/v2/orders"));
    }

    @Test
    void multiLevelWildcardMatchesRemaining() {
        TopicFilter f = new TopicFilter(List.of("rest/>"), List.of());
        assertTrue(f.accept("rest/v1/orders/123"));
    }

    @Test
    void p2pExclusionWorks() {
        TopicFilter f = new TopicFilter(List.of(">"), List.of("#P2P/>"));
        assertFalse(f.accept("#P2P/QUE/some-queue"));
        assertTrue(f.accept("rest/v1/orders"));
    }
}
