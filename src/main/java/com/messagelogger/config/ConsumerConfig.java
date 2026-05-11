package com.messagelogger.config;

import java.util.List;
import java.util.ArrayList;

public class ConsumerConfig {
    private String mode = "GUARANTEED";
    private GuaranteedConfig guaranteed = new GuaranteedConfig();
    private DirectConfig direct = new DirectConfig();
    private OutputConfig output = new OutputConfig();
    private PipelineConfig pipeline = new PipelineConfig();

    public static class GuaranteedConfig {
        private QueueConfig queue = new QueueConfig();

        public static class QueueConfig {
            private String name = "Q/logger";
            private String accessType = "EXCLUSIVE";
            private String ackMode = "CLIENT";
            private int maxRedelivery = 5;
            private int flowWindowSize = 255;
            private int transportWindowSize = 255;
            private String startState = "ENABLED";
            private boolean provision = false;
            private boolean respectExisting = true;
            private List<SubscriptionConfig> subscriptions = new ArrayList<>();

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getAccessType() {
                return accessType;
            }

            public void setAccessType(String accessType) {
                this.accessType = accessType;
            }

            public String getAckMode() {
                return ackMode;
            }

            public void setAckMode(String ackMode) {
                this.ackMode = ackMode;
            }

            public int getMaxRedelivery() {
                return maxRedelivery;
            }

            public void setMaxRedelivery(int maxRedelivery) {
                this.maxRedelivery = maxRedelivery;
            }

            public int getFlowWindowSize() {
                return flowWindowSize;
            }

            public void setFlowWindowSize(int flowWindowSize) {
                this.flowWindowSize = flowWindowSize;
            }

            public int getTransportWindowSize() {
                return transportWindowSize;
            }

            public void setTransportWindowSize(int transportWindowSize) {
                this.transportWindowSize = transportWindowSize;
            }

            public String getStartState() {
                return startState;
            }

            public void setStartState(String startState) {
                this.startState = startState;
            }

            public boolean isProvision() {
                return provision;
            }

            public void setProvision(boolean provision) {
                this.provision = provision;
            }

            public boolean isRespectExisting() {
                return respectExisting;
            }

            public void setRespectExisting(boolean respectExisting) {
                this.respectExisting = respectExisting;
            }

            public List<SubscriptionConfig> getSubscriptions() {
                return subscriptions;
            }

            public void setSubscriptions(List<SubscriptionConfig> subscriptions) {
                this.subscriptions = subscriptions;
            }
        }

        public static class SubscriptionConfig {
            private String topic;
            private boolean addOnStartup = false;

            public String getTopic() {
                return topic;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }

            public boolean isAddOnStartup() {
                return addOnStartup;
            }

            public void setAddOnStartup(boolean addOnStartup) {
                this.addOnStartup = addOnStartup;
            }
        }

        public QueueConfig getQueue() {
            return queue;
        }

        public void setQueue(QueueConfig queue) {
            this.queue = queue;
        }
    }

    public static class DirectConfig {
        private List<String> subscriptions = new ArrayList<>();

        public List<String> getSubscriptions() {
            return subscriptions;
        }

        public void setSubscriptions(List<String> subscriptions) {
            this.subscriptions = subscriptions;
        }
    }

    public static class OutputConfig {
        private boolean enabled = true;
        private String directory = "./logs/messages";
        private String filePattern = "messages-{yyyyMMdd}.{format}";
        private String format = "jsonl";
        private String rolling = "daily";
        private int maxSizeMb = 500;
        private int maxHistoryDays = 90;
        private String encoding = "UTF-8";
        private String captureSchema = "./config/capture-schema.json";
        private DelimitedConfig delimited = new DelimitedConfig();
        private LogFormatConfig logFormat = new LogFormatConfig();
        private FlushConfig flush = new FlushConfig();
        private FilterConfig filter = new FilterConfig();

        public static class DelimitedConfig {
            private String delimiter = "|";
            private String quoteChar = "\"";
            private boolean includeHeader = true;

            public String getDelimiter() {
                return delimiter;
            }

            public void setDelimiter(String delimiter) {
                this.delimiter = delimiter;
            }

            public String getQuoteChar() {
                return quoteChar;
            }

            public void setQuoteChar(String quoteChar) {
                this.quoteChar = quoteChar;
            }

            public boolean isIncludeHeader() {
                return includeHeader;
            }

            public void setIncludeHeader(boolean includeHeader) {
                this.includeHeader = includeHeader;
            }
        }

        public static class LogFormatConfig {
            private String pattern = "{timestamp} [{delivery_mode}] topic={destination_topic} msgId={message_id} size={payload_size_bytes} method={http_method} uri={http_uri} payload={payload}";

            public String getPattern() {
                return pattern;
            }

            public void setPattern(String pattern) {
                this.pattern = pattern;
            }
        }

        public static class FlushConfig {
            private int everyRecords = 1000;
            private int everyMs = 500;
            private boolean fsyncOnRotate = true;

            public int getEveryRecords() {
                return everyRecords;
            }

            public void setEveryRecords(int everyRecords) {
                this.everyRecords = everyRecords;
            }

            public int getEveryMs() {
                return everyMs;
            }

            public void setEveryMs(int everyMs) {
                this.everyMs = everyMs;
            }

            public boolean isFsyncOnRotate() {
                return fsyncOnRotate;
            }

            public void setFsyncOnRotate(boolean fsyncOnRotate) {
                this.fsyncOnRotate = fsyncOnRotate;
            }
        }

        public static class FilterConfig {
            private List<String> includeTopics = new ArrayList<>(List.of(">"));
            private List<String> excludeTopics = new ArrayList<>();
            private int maxPayloadBytes = 1048576;
            private boolean truncatePayload = true;
            private boolean skipEmptyPayload = false;

            public List<String> getIncludeTopics() {
                return includeTopics;
            }

            public void setIncludeTopics(List<String> includeTopics) {
                this.includeTopics = includeTopics;
            }

            public List<String> getExcludeTopics() {
                return excludeTopics;
            }

            public void setExcludeTopics(List<String> excludeTopics) {
                this.excludeTopics = excludeTopics;
            }

            public int getMaxPayloadBytes() {
                return maxPayloadBytes;
            }

            public void setMaxPayloadBytes(int maxPayloadBytes) {
                this.maxPayloadBytes = maxPayloadBytes;
            }

            public boolean isTruncatePayload() {
                return truncatePayload;
            }

            public void setTruncatePayload(boolean truncatePayload) {
                this.truncatePayload = truncatePayload;
            }

            public boolean isSkipEmptyPayload() {
                return skipEmptyPayload;
            }

            public void setSkipEmptyPayload(boolean skipEmptyPayload) {
                this.skipEmptyPayload = skipEmptyPayload;
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getFilePattern() {
            return filePattern;
        }

        public void setFilePattern(String filePattern) {
            this.filePattern = filePattern;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getRolling() {
            return rolling;
        }

        public void setRolling(String rolling) {
            this.rolling = rolling;
        }

        public int getMaxSizeMb() {
            return maxSizeMb;
        }

        public void setMaxSizeMb(int maxSizeMb) {
            this.maxSizeMb = maxSizeMb;
        }

        public int getMaxHistoryDays() {
            return maxHistoryDays;
        }

        public void setMaxHistoryDays(int maxHistoryDays) {
            this.maxHistoryDays = maxHistoryDays;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getCaptureSchema() {
            return captureSchema;
        }

        public void setCaptureSchema(String captureSchema) {
            this.captureSchema = captureSchema;
        }

        public DelimitedConfig getDelimited() {
            return delimited;
        }

        public void setDelimited(DelimitedConfig delimited) {
            this.delimited = delimited;
        }

        public LogFormatConfig getLogFormat() {
            return logFormat;
        }

        public void setLogFormat(LogFormatConfig logFormat) {
            this.logFormat = logFormat;
        }

        public FlushConfig getFlush() {
            return flush;
        }

        public void setFlush(FlushConfig flush) {
            this.flush = flush;
        }

        public FilterConfig getFilter() {
            return filter;
        }

        public void setFilter(FilterConfig filter) {
            this.filter = filter;
        }
    }

    public static class PipelineConfig {
        private int consumerThreads = 1;
        private int writerThreads = 2;
        private int internalQueueCapacity = 50000;
        private int batchSize = 500;
        private int batchTimeoutMs = 100;

        public int getConsumerThreads() {
            return consumerThreads;
        }

        public void setConsumerThreads(int consumerThreads) {
            this.consumerThreads = consumerThreads;
        }

        public int getWriterThreads() {
            return writerThreads;
        }

        public void setWriterThreads(int writerThreads) {
            this.writerThreads = writerThreads;
        }

        public int getInternalQueueCapacity() {
            return internalQueueCapacity;
        }

        public void setInternalQueueCapacity(int internalQueueCapacity) {
            this.internalQueueCapacity = internalQueueCapacity;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getBatchTimeoutMs() {
            return batchTimeoutMs;
        }

        public void setBatchTimeoutMs(int batchTimeoutMs) {
            this.batchTimeoutMs = batchTimeoutMs;
        }
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public GuaranteedConfig getGuaranteed() {
        return guaranteed;
    }

    public void setGuaranteed(GuaranteedConfig guaranteed) {
        this.guaranteed = guaranteed;
    }

    public DirectConfig getDirect() {
        return direct;
    }

    public void setDirect(DirectConfig direct) {
        this.direct = direct;
    }

    public OutputConfig getOutput() {
        return output;
    }

    public void setOutput(OutputConfig output) {
        this.output = output;
    }

    public PipelineConfig getPipeline() {
        return pipeline;
    }

    public void setPipeline(PipelineConfig pipeline) {
        this.pipeline = pipeline;
    }
}
