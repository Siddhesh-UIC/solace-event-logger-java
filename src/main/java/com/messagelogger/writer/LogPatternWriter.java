package com.messagelogger.writer;

import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.*;

public class LogPatternWriter extends AbstractFileWriter {
    private static final Logger log = LoggerFactory.getLogger(LogPatternWriter.class);
    private static final Pattern PLACEHOLDER = Pattern.compile("(\\S*\\{(\\w+)\\}\\S*)");

    private final String pattern;
    private final Set<String> enabledNames;

    public LogPatternWriter(OutputConfig cfg, List<CaptureSchema.FieldDefinition> enabledFields,
                            ZoneId zoneId) throws IOException {
        super(cfg, enabledFields, zoneId, "log");
        this.pattern = cfg.getLogFormat().getPattern();
        this.enabledNames = new HashSet<>();
        for (CaptureSchema.FieldDefinition f : enabledFields) enabledNames.add(f.getName());
        Matcher m = Pattern.compile("\\{(\\w+)\\}").matcher(pattern);
        while (m.find()) {
            if (!enabledNames.contains(m.group(1))) {
                log.warn("log-format.pattern references disabled field '{}' — token will be dropped", m.group(1));
            }
        }
    }

    @Override
    public synchronized void write(List<MessageRecord> batch) throws IOException {
        for (MessageRecord rec : batch) writeLine(buildLine(rec));
    }

    private String buildLine(MessageRecord rec) {
        Matcher m = PLACEHOLDER.matcher(pattern);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(2);
            if (!enabledNames.contains(fieldName)) {
                m.appendReplacement(sb, "");
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1)));
            }
        }
        m.appendTail(sb);
        String result = sb.toString().replaceAll("\\s{2,}", " ").trim();

        for (CaptureSchema.FieldDefinition field : enabledFields) {
            String value = rec.getValue(field.getName());
            result = result.replace("{" + field.getName() + "}", value == null ? "" : value);
        }
        return result;
    }
}
