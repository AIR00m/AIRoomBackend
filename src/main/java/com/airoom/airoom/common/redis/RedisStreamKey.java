package com.airoom.airoom.common.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisStreamKey {
    NOTIFICATION_STREAM("notification_stream");
    private final String key;
}
