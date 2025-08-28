package com.airoom.airoom.common.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisStreamKey {
    NOTIFICATION_STREAM("notification_stream"),
    CONSUMER_GROUP("notification_group"),
    CONSUMER_NAME("notification_consumer");

    private final String key;
}
