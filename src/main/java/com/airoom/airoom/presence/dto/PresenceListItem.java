package com.airoom.airoom.presence.dto;

/** 스냅샷 응답 DTO (lastSeen 포함) */
public record PresenceListItem(String userId, Long classId, boolean online, long lastSeen, String userName) {}
