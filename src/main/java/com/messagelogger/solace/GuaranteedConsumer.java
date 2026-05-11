package com.messagelogger.solace;

import com.messagelogger.config.ConsumerConfig.GuaranteedConfig;
import com.messagelogger.pipeline.MessagePipeline;
import com.messagelogger.processor.MessageMapper;
import com.messagelogger.processor.TopicFilter;
import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuaranteedConsumer {
    private static final Logger log = LoggerFactory.getLogger(GuaranteedConsumer.class);

    private final JCSMPSession session;
    private final GuaranteedConfig cfg;
    private final MessagePipeline pipeline;
    private final TopicFilter filter;
    private FlowReceiver flowReceiver;

    public GuaranteedConsumer(JCSMPSession session, GuaranteedConfig cfg,
                               MessagePipeline pipeline, TopicFilter filter) {
        this.session  = session;
        this.cfg      = cfg;
        this.pipeline = pipeline;
        this.filter   = filter;
    }

    public void start() throws JCSMPException {
        GuaranteedConfig.QueueConfig qCfg = cfg.getQueue();
        String queueName = qCfg.getName();

        if (qCfg.isProvision()) {
            provisionQueue(queueName, qCfg);
        }

        for (GuaranteedConfig.SubscriptionConfig sub : qCfg.getSubscriptions()) {
            if (sub.isAddOnStartup()) {
                session.addSubscription(
                    JCSMPFactory.onlyInstance().createQueue(queueName),
                    JCSMPFactory.onlyInstance().createTopic(sub.getTopic()),
                    JCSMPSession.WAIT_FOR_CONFIRM);
                log.info("Added subscription '{}' to queue '{}'", sub.getTopic(), queueName);
            }
        }

        ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
        flowProps.setEndpoint(JCSMPFactory.onlyInstance().createQueue(queueName));
        flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        flowProps.setStartState(true);

        flowReceiver = session.createFlow(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage msg) {
                String topic = msg.getDestination() != null ? msg.getDestination().getName() : "";
                if (!filter.accept(topic)) {
                    try { msg.ackMessage(); } catch (Exception ignored) {}
                    return;
                }
                try {
                    pipeline.submit(MessageMapper.map(msg, queueName, false));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            @Override
            public void onException(JCSMPException e) {
                log.error("Flow exception: {}", e.getMessage());
            }
        }, flowProps);

        flowReceiver.start();
        log.info("Bound to queue '{}' access={} provision={}", queueName,
            qCfg.getAccessType(), qCfg.isProvision());
    }

    public void stop() {
        if (flowReceiver != null) {
            flowReceiver.stop();
            flowReceiver.close();
        }
    }

    private void provisionQueue(String name, GuaranteedConfig.QueueConfig qCfg) throws JCSMPException {
        EndpointProperties ep = new EndpointProperties();
        ep.setPermission(EndpointProperties.PERMISSION_CONSUME);
        ep.setAccessType("EXCLUSIVE".equals(qCfg.getAccessType())
            ? EndpointProperties.ACCESSTYPE_EXCLUSIVE
            : EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
        Queue queue = JCSMPFactory.onlyInstance().createQueue(name);
        session.provision(queue, ep,
            qCfg.isRespectExisting() ? JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS : 0);
        log.info("Provisioned queue '{}'", name);
    }
}
