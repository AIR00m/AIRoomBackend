package com.airoom.airoom.common.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisStreamKey {
    ASSIGNMENT_PUB("assignment_pub");
    private final String key;
}
