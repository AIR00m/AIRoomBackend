package com.airoom.airoom.secureagent.model.dto;

public class ForensicEventRequest {
    private String type;       // "forensic" 등 (옵션)
    private String token;      // 12-hex
    private String encPayload; // AES-Base64
    private String agentTs;    // 클라이언트 시각(옵션)
    private String agentVer;   // 에이전트 버전(옵션)

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEncPayload() { return encPayload; }
    public void setEncPayload(String encPayload) { this.encPayload = encPayload; }
    public String getAgentTs() { return agentTs; }
    public void setAgentTs(String agentTs) { this.agentTs = agentTs; }
    public String getAgentVer() { return agentVer; }
    public void setAgentVer(String agentVer) { this.agentVer = agentVer; }
}
