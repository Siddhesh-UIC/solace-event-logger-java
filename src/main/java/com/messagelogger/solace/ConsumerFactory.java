package com.messagelogger.solace;

import com.messagelogger.config.AppConfig;
import com.messagelogger.pipeline.MessagePipeline;
import com.messagelogger.processor.TopicFilter;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;

public class ConsumerFactory {
    private GuaranteedConsumer guaranteed;
    private DirectConsumer direct;

    public void start(AppConfig cfg, JCSMPSession session,
                      MessagePipeline pipeline, TopicFilter filter) throws JCSMPException {
        switch (cfg.getConsumer().getMode()) {
            case "GUARANTEED" -> {
                guaranteed = new GuaranteedConsumer(session, cfg.getConsumer().getGuaranteed(),
                                                    pipeline, filter);
                guaranteed.start();
            }
            case "DIRECT" -> {
                direct = new DirectConsumer(session, cfg.getConsumer().getDirect(),
                                            pipeline, filter);
                direct.start();
            }
            default -> throw new IllegalArgumentException(
                "Unknown consumer mode: " + cfg.getConsumer().getMode());
        }
    }

    public void stop() {
        if (guaranteed != null) guaranteed.stop();
        if (direct != null) direct.stop();
    }
}
