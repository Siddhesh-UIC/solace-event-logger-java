package com.messagelogger.processor;

import java.util.List;

public class TopicFilter {
    private final List<String> includePatterns;
    private final List<String> excludePatterns;

    public TopicFilter(List<String> includePatterns, List<String> excludePatterns) {
        this.includePatterns = List.copyOf(includePatterns);
        this.excludePatterns = List.copyOf(excludePatterns);
    }

    public boolean accept(String topic) {
        if (topic == null) return false;
        boolean included = includePatterns.isEmpty() ||
            includePatterns.stream().anyMatch(p -> matches(topic, p));
        if (!included) return false;
        return excludePatterns.stream().noneMatch(p -> matches(topic, p));
    }

    private boolean matches(String topic, String pattern) {
        return matchParts(topic.split("/", -1), 0, pattern.split("/", -1), 0);
    }

    private boolean matchParts(String[] topic, int ti, String[] pattern, int pi) {
        if (pi == pattern.length) return ti == topic.length;
        if (">".equals(pattern[pi])) return true;
        if (ti == topic.length) return false;
        if ("*".equals(pattern[pi]) || pattern[pi].equals(topic[ti])) {
            return matchParts(topic, ti + 1, pattern, pi + 1);
        }
        return false;
    }
}
