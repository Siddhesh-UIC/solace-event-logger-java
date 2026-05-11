package com.messagelogger.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private TimeUtil() {}

    public static String nowUtc() {
        return FMT.format(Instant.now());
    }

    public static String formatUtc(Long epochMs) {
        if (epochMs == null || epochMs == 0) return null;
        return FMT.format(Instant.ofEpochMilli(epochMs));
    }
}
