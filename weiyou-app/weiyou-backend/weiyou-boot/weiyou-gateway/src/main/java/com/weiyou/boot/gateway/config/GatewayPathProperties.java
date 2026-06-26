package com.weiyou.boot.gateway.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weiyou.gateway")
public class GatewayPathProperties {

    private List<String> publicPaths = new ArrayList<>();
    private RateLimit rateLimit = new RateLimit();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public static class RateLimit {

        private boolean enabled = true;
        private int maxRequests = 120;
        private int windowSeconds = 60;
        private IdentityMode identityMode = IdentityMode.AUTO;
        private List<String> includePaths = new ArrayList<>(List.of("/api/**", "/ws/**"));
        private List<String> excludePaths = new ArrayList<>(List.of("/actuator/**"));
        private List<Rule> rules = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }

        public IdentityMode getIdentityMode() {
            return identityMode;
        }

        public void setIdentityMode(IdentityMode identityMode) {
            this.identityMode = identityMode;
        }

        public List<String> getIncludePaths() {
            return includePaths;
        }

        public void setIncludePaths(List<String> includePaths) {
            this.includePaths = includePaths;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }

        public List<Rule> getRules() {
            return rules;
        }

        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }
    }

    public static class Rule {

        private String path;
        private Integer maxRequests;
        private Integer windowSeconds;
        private IdentityMode identityMode;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Integer getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(Integer maxRequests) {
            this.maxRequests = maxRequests;
        }

        public Integer getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(Integer windowSeconds) {
            this.windowSeconds = windowSeconds;
        }

        public IdentityMode getIdentityMode() {
            return identityMode;
        }

        public void setIdentityMode(IdentityMode identityMode) {
            this.identityMode = identityMode;
        }
    }

    public enum IdentityMode {
        AUTO,
        IP,
        TOKEN,
        USER_ID
    }
}
