package com.messagelogger;

import com.messagelogger.config.*;
import com.messagelogger.pipeline.MessagePipeline;
import com.messagelogger.processor.TopicFilter;
import com.messagelogger.solace.ConsumerFactory;
import com.messagelogger.solace.SolaceConnector;
import com.messagelogger.writer.MessageWriter;
import com.messagelogger.writer.WriterFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public class MessageLoggerApp {
    private static final Logger log = LoggerFactory.getLogger(MessageLoggerApp.class);

    public static void main(String[] args) throws Exception {
        String configPath = resolveConfigPath(args);
        AppConfig cfg = ConfigLoader.load(Path.of(configPath), System.getenv());
        ConfigValidator.validate(cfg);

        String version = readVersion();
        log.info("Starting solace-message-logger v{} instance={} mode={}",
            version, cfg.getService().getInstanceId(), cfg.getConsumer().getMode());

        if (cfg.getService().getConfigVersion() != 1) {
            log.warn("config-version={} but service expects 1", cfg.getService().getConfigVersion());
        }

        CaptureSchema schema = CaptureSchemaLoader.load(
            Path.of(cfg.getConsumer().getOutput().getCaptureSchema()));
        List<CaptureSchema.FieldDefinition> enabledFields = schema.enabledFields();

        MessageWriter writer = WriterFactory.create(cfg, enabledFields);

        var pCfg = cfg.getConsumer().getPipeline();
        MessagePipeline pipeline = new MessagePipeline(
            pCfg.getInternalQueueCapacity(), pCfg.getWriterThreads(),
            pCfg.getBatchSize(), pCfg.getBatchTimeoutMs(), writer);
        pipeline.start();

        var fCfg = cfg.getConsumer().getOutput().getFilter();
        TopicFilter filter = new TopicFilter(fCfg.getIncludeTopics(), fCfg.getExcludeTopics());

        SolaceConnector connector = new SolaceConnector(cfg.getSolace());
        JCSMPSession session = connector.connect();

        ConsumerFactory consumerFactory = new ConsumerFactory();
        consumerFactory.start(cfg, session, pipeline, filter);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received");
            try {
                consumerFactory.stop();
                log.info("Consumer stopped");
                pipeline.shutdown();
                log.info("Pipeline drained");
                writer.close();
                log.info("Writer closed");
                connector.disconnect();
                log.info("Service stopped");
            } catch (Exception e) {
                log.error("Error during shutdown: {}", e.getMessage());
            }
        }, "shutdown-hook"));

        log.info("Service ready");
    }

    private static String resolveConfigPath(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--config".equals(args[i])) return args[i + 1];
        }
        String envPath = System.getenv("CONFIG_PATH");
        return envPath != null ? envPath : "./config/application.yaml";
    }

    private static String readVersion() {
        try (var stream = MessageLoggerApp.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (stream == null) return "unknown";
            java.util.jar.Manifest manifest = new java.util.jar.Manifest(stream);
            String v = manifest.getMainAttributes().getValue("Implementation-Version");
            return v != null ? v : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
