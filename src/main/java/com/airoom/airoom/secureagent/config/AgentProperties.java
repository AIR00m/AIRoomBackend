package com.airoom.airoom.secureagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    private List<String> allowedVersions = List.of();
    private List<String> allowedHashes = List.of();

    public List<String> getAllowedVersions() { return allowedVersions; }
    public void setAllowedVersions(List<String> allowedVersions) { this.allowedVersions = allowedVersions; }

    public List<String> getAllowedHashes() { return allowedHashes; }
    public void setAllowedHashes(List<String> allowedHashes) { this.allowedHashes = allowedHashes; }
}