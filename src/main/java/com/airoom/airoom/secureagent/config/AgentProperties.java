package com.airoom.airoom.secureagent.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    private List<String> allowedVersions = List.of();
    private List<String> allowedHashes = List.of();

    public List<String> getAllowedVersions() { return allowedVersions; }
    public void setAllowedVersions(List<String> allowedVersions) {
        this.allowedVersions = (allowedVersions == null ? List.of() : allowedVersions);
    }
    public List<String> getAllowedHashes() { return allowedHashes; }
    public void setAllowedHashes(List<String> allowedHashes) {
        this.allowedHashes = (allowedHashes == null ? List.of() : allowedHashes);
    }
}
