package com.messagelogger.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.messagelogger.util.JsonUtil;
import com.messagelogger.util.TimeUtil;
import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageMapper {
    private static final Logger log = LoggerFactory.getLogger(MessageMapper.class);

    /**
     * @param msg        JCSMP message received
     * @param queueName  bound queue name, or null in DIRECT mode
     * @param directMode true if DIRECT mode (ackHandle will be null, queue_name/rgmid null)
     */
    public static MessageRecord map(BytesXMLMessage msg, String queueName, boolean directMode) {
        String now = TimeUtil.nowUtc();

        // Timestamps
        String senderTs = TimeUtil.formatUtc(msg.getSenderTimestamp());

        // Destination
        String destination = msg.getDestination() != null ? msg.getDestination().getName() : null;

        // Delivery mode
        String deliveryMode = msg.getDeliveryMode() != null ? msg.getDeliveryMode().name() : null;

        // RGMID (guaranteed only)
        String rgmid = null;
        if (!directMode) {
            try {
                ReplicationGroupMessageId rgmidObj = msg.getReplicationGroupMessageId();
                if (rgmidObj != null) rgmid = rgmidObj.toString();
            } catch (Exception ignored) {}
        }

        // User properties (SDT map)
        SDTMap props = msg.getProperties();
        String userProps = serializeUserProperties(props);

        // HTTP-injected user properties from MicroGateway
        String httpMethod = safeGet(props, "http-method");
        String httpUri    = safeGet(props, "http-target-uri");
        String httpStatus = safeGet(props, "http-status-code");

        // Payload
        byte[] raw = msg.getBytes();
        String payload;
        String payloadEncoding = null;
        if (raw == null || raw.length == 0) {
            payload = null;
        } else {
            try {
                JsonNode node = JsonUtil.MAPPER.readTree(raw);
                payload = JsonUtil.MAPPER.writeValueAsString(node);
            } catch (Exception e) {
                payload = Base64.getEncoder().encodeToString(raw);
                payloadEncoding = "base64";
            }
        }

        // Headers
        Map<String, Object> hdrs = new LinkedHashMap<>();
        String httpContentType = msg.getHTTPContentType();
        if (httpContentType != null) hdrs.put("contentType", httpContentType);
        String headers = hdrs.isEmpty() ? "{}" : JsonUtil.toCompactJson(hdrs);

        // Priority
        String priority = null;
        try {
            int p = msg.getPriority();
            if (p > 0) priority = String.valueOf(p);
        } catch (Exception ignored) {}

        // Expiration
        String expiration = null;
        try {
            long exp = msg.getExpiration();
            if (exp > 0) expiration = String.valueOf(exp);
        } catch (Exception ignored) {}

        AckHandle ackHandle = directMode ? null : new AckHandle(msg);

        return new MessageRecord(
            now, now, senderTs, null,
            msg.getMessageId(), msg.getCorrelationId(), rgmid,
            destination, queueName, deliveryMode,
            priority, String.valueOf(msg.getRedelivered()), String.valueOf(msg.isDMQEligible()),
            expiration, String.valueOf(msg.getAttachmentContentLength()),
            httpContentType, httpMethod, httpUri, httpStatus,
            headers, userProps, payload, payloadEncoding,
            ackHandle
        );
    }

    private static String serializeUserProperties(SDTMap sdtMap) {
        if (sdtMap == null) return "{}";
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            for (String key : sdtMap.keySet()) {
                try { map.put(key, sdtMap.get(key)); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.debug("Failed to read user properties: {}", e.getMessage());
        }
        return JsonUtil.toCompactJson(map);
    }

    private static String safeGet(SDTMap props, String key) {
        if (props == null) return null;
        try {
            Object v = props.get(key);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
