package com.messagelogger.config;

public class AppConfig {
    private ServiceConfig service = new ServiceConfig();
    private SolaceConfig solace = new SolaceConfig();
    private LoggingConfig logging = new LoggingConfig();
    private ConsumerConfig consumer = new ConsumerConfig();

    public ServiceConfig getService() {
        return service;
    }

    public void setService(ServiceConfig service) {
        this.service = service;
    }

    public SolaceConfig getSolace() {
        return solace;
    }

    public void setSolace(SolaceConfig solace) {
        this.solace = solace;
    }

    public LoggingConfig getLogging() {
        return logging;
    }

    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }

    public ConsumerConfig getConsumer() {
        return consumer;
    }

    public void setConsumer(ConsumerConfig consumer) {
        this.consumer = consumer;
    }
}
