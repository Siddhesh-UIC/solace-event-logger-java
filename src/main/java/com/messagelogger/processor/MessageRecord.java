package com.messagelogger.processor;

public class MessageRecord {
    public final String timestamp;
    public final String receiveTimestamp;
    public final String senderTimestamp;
    public final String brokerTimestamp;
    public final String messageId;
    public final String correlationId;
    public final String replicationGroupMessageId;
    public final String destinationTopic;
    public final String queueName;
    public final String deliveryMode;
    public final String priority;
    public final String redelivered;
    public final String dmqEligible;
    public final String expiration;
    public final String payloadSizeBytes;
    public final String contentType;
    public final String httpMethod;
    public final String httpUri;
    public final String httpStatus;
    public final String headers;
    public final String userProperties;
    public final String payload;
    public final String payloadEncoding;  // "base64" or null
    public final AckHandle ackHandle;     // null in DIRECT mode

    public MessageRecord(String timestamp, String receiveTimestamp, String senderTimestamp,
                         String brokerTimestamp, String messageId, String correlationId,
                         String replicationGroupMessageId, String destinationTopic,
                         String queueName, String deliveryMode, String priority,
                         String redelivered, String dmqEligible, String expiration,
                         String payloadSizeBytes, String contentType, String httpMethod,
                         String httpUri, String httpStatus, String headers,
                         String userProperties, String payload, String payloadEncoding,
                         AckHandle ackHandle) {
        this.timestamp = timestamp;
        this.receiveTimestamp = receiveTimestamp;
        this.senderTimestamp = senderTimestamp;
        this.brokerTimestamp = brokerTimestamp;
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.replicationGroupMessageId = replicationGroupMessageId;
        this.destinationTopic = destinationTopic;
        this.queueName = queueName;
        this.deliveryMode = deliveryMode;
        this.priority = priority;
        this.redelivered = redelivered;
        this.dmqEligible = dmqEligible;
        this.expiration = expiration;
        this.payloadSizeBytes = payloadSizeBytes;
        this.contentType = contentType;
        this.httpMethod = httpMethod;
        this.httpUri = httpUri;
        this.httpStatus = httpStatus;
        this.headers = headers;
        this.userProperties = userProperties;
        this.payload = payload;
        this.payloadEncoding = payloadEncoding;
        this.ackHandle = ackHandle;
    }

    /** Schema-driven field lookup used by all writers. Returns null for unknown names. */
    public String getValue(String fieldName) {
        return switch (fieldName) {
            case "timestamp"                    -> timestamp;
            case "receive_timestamp"            -> receiveTimestamp;
            case "sender_timestamp"             -> senderTimestamp;
            case "broker_timestamp"             -> brokerTimestamp;
            case "message_id"                   -> messageId;
            case "correlation_id"               -> correlationId;
            case "replication_group_message_id" -> replicationGroupMessageId;
            case "destination_topic"            -> destinationTopic;
            case "queue_name"                   -> queueName;
            case "delivery_mode"                -> deliveryMode;
            case "priority"                     -> priority;
            case "redelivered"                  -> redelivered;
            case "dmq_eligible"                 -> dmqEligible;
            case "expiration"                   -> expiration;
            case "payload_size_bytes"           -> payloadSizeBytes;
            case "content_type"                 -> contentType;
            case "http_method"                  -> httpMethod;
            case "http_uri"                     -> httpUri;
            case "http_status"                  -> httpStatus;
            case "headers"                      -> headers;
            case "user_properties"              -> userProperties;
            case "payload"                      -> payload;
            default                             -> null;
        };
    }
}
