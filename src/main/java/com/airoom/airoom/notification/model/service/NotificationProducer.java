package com.airoom.airoom.notification.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RedisTemplate<Long,Object> redisTemplate;



}
