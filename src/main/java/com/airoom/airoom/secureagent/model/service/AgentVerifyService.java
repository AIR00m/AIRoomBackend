package com.airoom.airoom.secureagent.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentVerifyService {

    @Value("${agent.allowed-versions}")
    private List<String> allowedVersions;

    @Value("${agent.allowed-hashes}")
    private List<String> allowedHashes;

    public boolean isAllowed(String version, String sha256) {
        return allowedVersions.contains(version) && allowedHashes.contains(sha256);
    }
}
