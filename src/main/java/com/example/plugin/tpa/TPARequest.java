package com.example.plugin.tpa;

import java.util.UUID;

/**
 * TPA 요청 정보를 저장하는 클래스
 */
public class TPARequest {
    private final UUID requesterId;
    private final UUID targetId;
    private final long timestamp;

    public TPARequest(UUID requesterId, UUID targetId, long timestamp) {
        this.requesterId = requesterId;
        this.targetId = targetId;
        this.timestamp = timestamp;
    }

    /**
     * 요청자 UUID 반환
     * @return 요청자 UUID
     */
    public UUID getRequesterId() {
        return requesterId;
    }

    /**
     * 대상자 UUID 반환
     * @return 대상자 UUID
     */
    public UUID getTargetId() {
        return targetId;
    }

    /**
     * 요청 시간 반환
     * @return 요청 시간 (밀리초)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 요청이 만료되었는지 확인
     * @param timeoutSeconds 만료 시간 (초)
     * @return 만료 여부
     */
    public boolean isExpired(int timeoutSeconds) {
        return (System.currentTimeMillis() - timestamp) > (timeoutSeconds * 1000);
    }
}
