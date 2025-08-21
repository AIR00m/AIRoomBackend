package com.airoom.airoom.secureagent.model.service;

import com.airoom.airoom.secureagent.config.AgentProperties;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AgentVerifyService {
    private final Set<String> allowedVersions;
    private final Set<String> allowedHashes;

    public AgentVerifyService(AgentProperties props) {
        this.allowedVersions = new HashSet<>(props.getAllowedVersions() == null ? List.of() : props.getAllowedVersions());
        this.allowedHashes   = new HashSet<>(props.getAllowedHashes()   == null ? List.of() : props.getAllowedHashes());
    }

    public boolean isAllowed(String version, String sha256) {
        if (version == null || sha256 == null) return false;
        boolean verOk  = allowedVersions.stream().anyMatch(v -> v.equalsIgnoreCase(version));
        boolean hashOk = allowedHashes.stream().anyMatch(h -> h.equalsIgnoreCase(sha256));
        return verOk && hashOk;
    }
}
