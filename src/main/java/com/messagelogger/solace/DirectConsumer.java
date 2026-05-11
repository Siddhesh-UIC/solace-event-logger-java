package com.messagelogger.solace;

import com.messagelogger.config.ConsumerConfig.DirectConfig;
import com.messagelogger.pipeline.MessagePipeline;
import com.messagelogger.processor.MessageMapper;
import com.messagelogger.processor.TopicFilter;
import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectConsumer {
    private static final Logger log = LoggerFactory.getLogger(DirectConsumer.class);

    private final JCSMPSession session;
    private final DirectConfig cfg;
    private final MessagePipeline pipeline;
    private final TopicFilter filter;
    private XMLMessageConsumer consumer;

    public DirectConsumer(JCSMPSession session, DirectConfig cfg,
                          MessagePipeline pipeline, TopicFilter filter) {
        this.session  = session;
        this.cfg      = cfg;
        this.pipeline = pipeline;
        this.filter   = filter;
    }

    public void start() throws JCSMPException {
        consumer = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage msg) {
                String topic = msg.getDestination() != null ? msg.getDestination().getName() : "";
                if (!filter.accept(topic)) return;
                try {
                    pipeline.submit(MessageMapper.map(msg, null, true));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            @Override
            public void onException(JCSMPException e) {
                log.error("Direct consumer exception: {}", e.getMessage());
            }
        });

        for (String topic : cfg.getSubscriptions()) {
            session.addSubscription(JCSMPFactory.onlyInstance().createTopic(topic));
            log.info("Added direct subscription '{}'", topic);
        }
        consumer.start();
        log.info("Direct consumer started, subscriptions={}", cfg.getSubscriptions().size());
    }

    public void stop() {
        if (consumer != null) {
            consumer.stop();
            consumer.close();
        }
    }
}
