package com.messagelogger.solace;

import com.messagelogger.config.SolaceConfig;
import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolaceConnector {
    private static final Logger log = LoggerFactory.getLogger(SolaceConnector.class);

    private final SolaceConfig cfg;
    private JCSMPSession session;

    public SolaceConnector(SolaceConfig cfg) {
        this.cfg = cfg;
    }

    public JCSMPSession connect() throws JCSMPException {
        JCSMPProperties props = new JCSMPProperties();
        props.setProperty(JCSMPProperties.HOST,        cfg.getHost());
        props.setProperty(JCSMPProperties.VPN_NAME,    cfg.getVpn());
        props.setProperty(JCSMPProperties.USERNAME,    cfg.getUsername());
        props.setProperty(JCSMPProperties.PASSWORD,    cfg.getPassword());
        props.setProperty(JCSMPProperties.CLIENT_NAME, cfg.getClientName());

        SolaceConfig.ConnectionConfig conn = cfg.getConnection();
        JCSMPChannelProperties channelProps = new JCSMPChannelProperties();
        channelProps.setConnectRetries(conn.getConnectRetries());
        channelProps.setReconnectRetries(conn.getReconnectRetries());
        channelProps.setReconnectRetryWaitInMillis(conn.getReconnectRetryWaitMs());
        channelProps.setKeepAliveIntervalInMillis(conn.getKeepAliveIntervalMs());
        channelProps.setKeepAliveLimit(conn.getKeepAliveLimit());
        channelProps.setTcpNoDelay(conn.isTcpNoDelay());
        channelProps.setCompressionLevel(conn.getCompressionLevel());
        props.setProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES, channelProps);

        log.info("Connecting to {} vpn={} user={}", cfg.getHost(), cfg.getVpn(), cfg.getUsername());
        session = JCSMPFactory.onlyInstance().createSession(props, null, new ConnectionEventHandler());
        session.connect();
        return session;
    }

    public void disconnect() {
        if (session != null && !session.isClosed()) {
            session.closeSession();
            log.info("Session closed");
        }
    }
}
