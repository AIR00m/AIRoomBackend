package com.airoom.airoom.secureagent.model.dto;

public class AgentVerifyResponse {
    private boolean ok;
    private String reason;

    public AgentVerifyResponse(boolean ok, String reason) {
        this.ok = ok; this.reason = reason;
    }
    public boolean isOk() { return ok; }
    public String getReason() { return reason; }
}
