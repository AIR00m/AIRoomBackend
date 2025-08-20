package com.airoom.airoom.secureagent.model.service;

import com.airoom.airoom.secureagent.config.AgentProperties;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AgentVerifyService {

    private final Set<String> allowedVersions;
    private final Set<String> allowedHashes;

    public AgentVerifyService(AgentProperties props) {
        this.allowedVersions = new HashSet<>(props.getAllowedVersions());
        this.allowedHashes   = new HashSet<>(props.getAllowedHashes());
    }

    public boolean isAllowed(String version, String sha256) {
        return allowedVersions.contains(version) && allowedHashes.contains(sha256);
    }
}
