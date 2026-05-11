package com.messagelogger.config;

public class SolaceConfig {
    private String host = "tcp://broker.example.com:55555";
    private String vpn = "BVPN";
    private String username = "logger-svc";
    private String password;
    private String clientName;
    private ConnectionConfig connection = new ConnectionConfig();

    public static class ConnectionConfig {
        private int connectRetries = 5;
        private int connectRetriesPerHost = 3;
        private int reconnectRetries = -1;
        private int reconnectRetryWaitMs = 3000;
        private int keepAliveIntervalMs = 3000;
        private int keepAliveLimit = 3;
        private boolean tcpNoDelay = true;
        private int compressionLevel = 0;
        private SslConfig ssl = new SslConfig();

        public int getConnectRetries() {
            return connectRetries;
        }

        public void setConnectRetries(int connectRetries) {
            this.connectRetries = connectRetries;
        }

        public int getConnectRetriesPerHost() {
            return connectRetriesPerHost;
        }

        public void setConnectRetriesPerHost(int connectRetriesPerHost) {
            this.connectRetriesPerHost = connectRetriesPerHost;
        }

        public int getReconnectRetries() {
            return reconnectRetries;
        }

        public void setReconnectRetries(int reconnectRetries) {
            this.reconnectRetries = reconnectRetries;
        }

        public int getReconnectRetryWaitMs() {
            return reconnectRetryWaitMs;
        }

        public void setReconnectRetryWaitMs(int reconnectRetryWaitMs) {
            this.reconnectRetryWaitMs = reconnectRetryWaitMs;
        }

        public int getKeepAliveIntervalMs() {
            return keepAliveIntervalMs;
        }

        public void setKeepAliveIntervalMs(int keepAliveIntervalMs) {
            this.keepAliveIntervalMs = keepAliveIntervalMs;
        }

        public int getKeepAliveLimit() {
            return keepAliveLimit;
        }

        public void setKeepAliveLimit(int keepAliveLimit) {
            this.keepAliveLimit = keepAliveLimit;
        }

        public boolean isTcpNoDelay() {
            return tcpNoDelay;
        }

        public void setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        public int getCompressionLevel() {
            return compressionLevel;
        }

        public void setCompressionLevel(int compressionLevel) {
            this.compressionLevel = compressionLevel;
        }

        public SslConfig getSsl() {
            return ssl;
        }

        public void setSsl(SslConfig ssl) {
            this.ssl = ssl;
        }
    }

    public static class SslConfig {
        private boolean enabled = false;
        private String trustStore = "";
        private String trustStorePassword = "";
        private boolean validateCertificate = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(String trustStore) {
            this.trustStore = trustStore;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        public boolean isValidateCertificate() {
            return validateCertificate;
        }

        public void setValidateCertificate(boolean validateCertificate) {
            this.validateCertificate = validateCertificate;
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getVpn() {
        return vpn;
    }

    public void setVpn(String vpn) {
        this.vpn = vpn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public ConnectionConfig getConnection() {
        return connection;
    }

    public void setConnection(ConnectionConfig connection) {
        this.connection = connection;
    }
}
