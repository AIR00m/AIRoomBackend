package com.airoom.airoom.presence.dto;

/** 브로드캐스트용 가벼운 DTO */
public record PresenceEvent(String userId, Long classNo, boolean online, Long lastSeen) {}
