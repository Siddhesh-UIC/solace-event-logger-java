package com.messagelogger.config;

public class LoggingConfig {
    private ServiceLogConfig service = new ServiceLogConfig();

    public static class ServiceLogConfig {
        private boolean enabled = true;
        private String directory = "./logs/service";
        private String filePattern = "service-{yyyyMMdd}.log";
        private String level = "INFO";
        private String rolling = "daily";
        private int maxSizeMb = 100;
        private int maxHistoryDays = 30;
        private String encoding = "UTF-8";
        private String pattern = "[{timestamp}] [{level}] [{thread}] [{logger}] - {message}";
        private boolean consoleAlso = true;

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

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
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

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isConsoleAlso() {
            return consoleAlso;
        }

        public void setConsoleAlso(boolean consoleAlso) {
            this.consoleAlso = consoleAlso;
        }
    }

    public ServiceLogConfig getService() {
        return service;
    }

    public void setService(ServiceLogConfig service) {
        this.service = service;
    }
}
