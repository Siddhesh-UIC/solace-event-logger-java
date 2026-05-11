package com.messagelogger.writer;

import com.messagelogger.config.CaptureSchema;
import com.messagelogger.config.ConsumerConfig.OutputConfig;
import com.messagelogger.processor.MessageRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class AbstractFileWriter implements MessageWriter {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Path directory;
    private final String filePattern;
    private final String format;
    private final Charset charset;
    private final int maxHistoryDays;
    private final ZoneId zoneId;
    private final int flushEveryRecords;
    private final int flushEveryMs;
    private final boolean fsyncOnRotate;

    protected final List<CaptureSchema.FieldDefinition> enabledFields;

    private BufferedWriter writer;
    private FileOutputStream fos;
    private LocalDate currentDate;
    private long recordsSinceFlush;
    private long lastFlushMs;

    protected AbstractFileWriter(OutputConfig cfg, List<CaptureSchema.FieldDefinition> enabledFields,
                                  ZoneId zoneId, String format) throws IOException {
        this.directory       = Path.of(cfg.getDirectory());
        this.filePattern     = cfg.getFilePattern().replace("{format}", format);
        this.format          = format;
        this.charset         = Charset.forName(cfg.getEncoding());
        this.maxHistoryDays  = cfg.getMaxHistoryDays();
        this.zoneId          = zoneId;
        this.flushEveryRecords = cfg.getFlush().getEveryRecords();
        this.flushEveryMs    = cfg.getFlush().getEveryMs();
        this.fsyncOnRotate   = cfg.getFlush().isFsyncOnRotate();
        this.enabledFields   = enabledFields;
        Files.createDirectories(directory);
        openFile(LocalDate.now(zoneId));
    }

    private void openFile(LocalDate date) throws IOException {
        String filename = filePattern.replace("{yyyyMMdd}",
            date.format(DateTimeFormatter.BASIC_ISO_DATE));
        Path file = directory.resolve(filename);
        fos = new FileOutputStream(file.toFile(), true);
        writer = new BufferedWriter(new OutputStreamWriter(fos, charset));
        currentDate = date;
        lastFlushMs = System.currentTimeMillis();
        log.info("Output file opened: {} format={}", file, format);
        onFileOpened();
    }

    protected void onFileOpened() throws IOException {}

    protected void writeLine(String line) throws IOException {
        LocalDate today = LocalDate.now(zoneId);
        if (!today.equals(currentDate)) {
            rotate(today);
        }
        writer.write(line);
        writer.newLine();
        recordsSinceFlush++;
        long now = System.currentTimeMillis();
        if (recordsSinceFlush >= flushEveryRecords || now - lastFlushMs >= flushEveryMs) {
            doFlush();
        }
    }

    private synchronized void rotate(LocalDate newDate) throws IOException {
        log.info("Rotating output file -> {}", filePattern.replace("{yyyyMMdd}",
            newDate.format(DateTimeFormatter.BASIC_ISO_DATE)));
        doFlush();
        if (fsyncOnRotate) fos.getFD().sync();
        writer.close();
        deleteOldFiles();
        openFile(newDate);
    }

    private void doFlush() throws IOException {
        writer.flush();
        recordsSinceFlush = 0;
        lastFlushMs = System.currentTimeMillis();
    }

    private void deleteOldFiles() {
        LocalDate cutoff = LocalDate.now(zoneId).minusDays(maxHistoryDays);
        try (var stream = Files.list(directory)) {
            stream.filter(p -> {
                String name = p.getFileName().toString();
                try {
                    String datePart = name.replaceAll("[^0-9]", "").substring(0, 8);
                    LocalDate fileDate = LocalDate.parse(datePart, DateTimeFormatter.BASIC_ISO_DATE);
                    return fileDate.isBefore(cutoff);
                } catch (Exception e) { return false; }
            }).forEach(p -> {
                try { Files.delete(p); log.info("Deleted old output file: {}", p); }
                catch (IOException e) { log.warn("Could not delete {}: {}", p, e.getMessage()); }
            });
        } catch (IOException e) {
            log.warn("Could not list output directory for cleanup: {}", e.getMessage());
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (writer != null) doFlush();
    }

    @Override
    public synchronized void close() throws IOException {
        if (writer != null) {
            doFlush();
            writer.close();
            writer = null;
        }
    }

    @Override
    public abstract void write(List<MessageRecord> batch) throws IOException;
}
