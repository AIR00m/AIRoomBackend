package com.airoom.airoom.secureagent.model.dto;

public class AgentVerifyRequest {
    private String sha256;
    private String version;

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
