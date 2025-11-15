package org.think_ing.occupyplugin.tpa;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.think_ing.occupyplugin.OccupyPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TPA 요청 관리자
 * 텔레포트 요청을 생성, 관리, 만료 처리합니다
 */
public class TPAManager {

    private final OccupyPlugin plugin;
    private final Map<UUID, TPARequest> pendingRequests; // 수신자 UUID -> 요청
    private final Map<UUID, Long> cooldowns; // 플레이어 UUID -> 마지막 요청 시간

    public TPAManager(OccupyPlugin plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    /**
     * 새로운 TPA 요청 생성
     * @param requester 요청자
     * @param target 대상
     * @return 요청 생성 성공 여부
     */
    public boolean createRequest(Player requester, Player target) {
        UUID targetUUID = target.getUniqueId();

        // 이미 대기 중인 요청이 있으면 취소
        if (pendingRequests.containsKey(targetUUID)) {
            TPARequest oldRequest = pendingRequests.get(targetUUID);
            oldRequest.cancel();
        }

        // 새 요청 생성
        TPARequest request = new TPARequest(requester, target);
        pendingRequests.put(targetUUID, request);

        // 쿨다운 설정
        cooldowns.put(requester.getUniqueId(), System.currentTimeMillis());

        // 만료 타이머 시작
        startExpirationTimer(request);

        return true;
    }

    /**
     * 요청 수락
     * @param target 대상 (수신자)
     * @return 수락 성공 여부
     */
    public boolean acceptRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        TPARequest request = pendingRequests.get(targetUUID);

        if (request == null || request.isExpired()) {
            return false;
        }

        // 텔레포트 실행
        boolean success = request.teleport();

        // 요청 제거
        pendingRequests.remove(targetUUID);

        return success;
    }

    /**
     * 요청 거부
     * @param target 대상 (수신자)
     * @return 거부 성공 여부
     */
    public boolean denyRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        TPARequest request = pendingRequests.remove(targetUUID);

        if (request == null || request.isExpired()) {
            return false;
        }

        request.cancel();
        return true;
    }

    /**
     * 대기 중인 요청 조회
     * @param target 대상
     * @return 요청 (없으면 null)
     */
    public TPARequest getPendingRequest(Player target) {
        TPARequest request = pendingRequests.get(target.getUniqueId());
        if (request != null && request.isExpired()) {
            pendingRequests.remove(target.getUniqueId());
            return null;
        }
        return request;
    }

    /**
     * 쿨다운 확인
     * @param player 플레이어
     * @return 쿨다운 중이면 true
     */
    public boolean isOnCooldown(Player player) {
        Long lastRequest = cooldowns.get(player.getUniqueId());
        if (lastRequest == null) {
            return false;
        }

        int cooldownSeconds = plugin.getConfig().getInt("tpa.cooldown_seconds", 5);
        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsed = System.currentTimeMillis() - lastRequest;

        return elapsed < cooldownMillis;
    }

    /**
     * 남은 쿨다운 시간 조회 (초)
     * @param player 플레이어
     * @return 남은 시간 (초)
     */
    public int getRemainingCooldown(Player player) {
        Long lastRequest = cooldowns.get(player.getUniqueId());
        if (lastRequest == null) {
            return 0;
        }

        int cooldownSeconds = plugin.getConfig().getInt("tpa.cooldown_seconds", 5);
        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsed = System.currentTimeMillis() - lastRequest;
        long remaining = cooldownMillis - elapsed;

        return remaining > 0 ? (int) (remaining / 1000) + 1 : 0;
    }

    /**
     * 요청 만료 타이머 시작
     * @param request 요청
     */
    private void startExpirationTimer(TPARequest request) {
        int expirationSeconds = plugin.getConfig().getInt("tpa.expiration_seconds", 30);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!request.isExpired()) {
                    request.expire();
                    pendingRequests.remove(request.getTarget().getUniqueId());
                }
            }
        }.runTaskLater(plugin, expirationSeconds * 20L);
    }

    /**
     * 모든 요청 취소
     */
    public void clearAllRequests() {
        for (TPARequest request : pendingRequests.values()) {
            request.cancel();
        }
        pendingRequests.clear();
        cooldowns.clear();
    }

    /**
     * 요청 만료 시간 조회
     * @return 만료 시간 (초)
     */
    public int getExpirationSeconds() {
        return plugin.getConfig().getInt("tpa.expiration_seconds", 30);
    }
}

