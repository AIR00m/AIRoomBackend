package com.airoom.airoom.secureagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "agent.ingest")
public class IngestSecurityProperties {
    /** 에이전트와 동일 키 (초기값은 에이전트 CryptoUtil의 기본키) */
    private String aesKey = "AIDT2025UserKey!";
    /** PayloadManager에서 쓰는 토큰 시크릿 (운영에선 서버만 보관) */
    private String tokenSecret = "DEV_TOKEN_SECRET";

    public String getAesKey() { return aesKey; }
    public void setAesKey(String aesKey) { this.aesKey = aesKey; }

    public String getTokenSecret() { return tokenSecret; }
    public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }
}
