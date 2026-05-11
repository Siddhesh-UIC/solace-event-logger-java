package com.messagelogger.pipeline;

import com.messagelogger.processor.MessageRecord;
import com.messagelogger.writer.MessageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MessagePipeline {
    private static final Logger log = LoggerFactory.getLogger(MessagePipeline.class);

    private final BlockingQueue<MessageRecord> queue;
    private final int batchSize;
    private final int batchTimeoutMs;
    private final MessageWriter writer;
    private final ExecutorService writerPool;
    private volatile boolean running = false;

    public MessagePipeline(int queueCapacity, int writerThreads,
                           int batchSize, int batchTimeoutMs, MessageWriter writer) {
        this.queue          = new ArrayBlockingQueue<>(queueCapacity);
        this.batchSize      = batchSize;
        this.batchTimeoutMs = batchTimeoutMs;
        this.writer         = writer;
        this.writerPool     = Executors.newFixedThreadPool(writerThreads,
            r -> { Thread t = new Thread(r, "writer"); t.setDaemon(true); return t; });
    }

    public void start() {
        running = true;
        int threads = ((ThreadPoolExecutor) writerPool).getCorePoolSize();
        for (int i = 0; i < threads; i++) writerPool.submit(this::writerLoop);
        log.info("Pipeline started, writer-threads={}", threads);
    }

    public void submit(MessageRecord record) throws InterruptedException {
        queue.put(record);
    }

    private void writerLoop() {
        List<MessageRecord> batch = new ArrayList<>(batchSize);
        while (running || !queue.isEmpty()) {
            try {
                MessageRecord first = queue.poll(batchTimeoutMs, TimeUnit.MILLISECONDS);
                if (first != null) {
                    batch.add(first);
                    queue.drainTo(batch, batchSize - 1);
                }
                if (!batch.isEmpty()) {
                    writeBatch(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void writeBatch(List<MessageRecord> batch) {
        try {
            synchronized (writer) {
                writer.write(batch);
            }
            for (MessageRecord rec : batch) {
                if (rec.ackHandle != null) {
                    try { rec.ackHandle.ack(); }
                    catch (Exception e) {
                        log.error("Failed to ack messageId={} reason={}", rec.messageId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Write batch failed, size={} reason={}", batch.size(), e.getMessage());
        }
    }

    public void shutdown() throws InterruptedException {
        running = false;
        writerPool.shutdown();
        writerPool.awaitTermination(30, TimeUnit.SECONDS);
        try { writer.flush(); } catch (Exception e) { log.warn("Final flush failed: {}", e.getMessage()); }
        log.info("Pipeline shut down");
    }
}
