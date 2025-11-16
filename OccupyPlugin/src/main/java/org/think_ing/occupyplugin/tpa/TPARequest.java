package org.think_ing.occupyplugin.tpa;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * TPA 요청 모델
 * 텔레포트 요청 정보를 저장합니다
 */
public class TPARequest {

    private final Player requester;
    private final Player target;
    private final long createdTime;
    private boolean expired;
    private boolean cancelled;

    public TPARequest(Player requester, Player target) {
        this.requester = requester;
        this.target = target;
        this.createdTime = System.currentTimeMillis();
        this.expired = false;
        this.cancelled = false;
    }

    /**
     * 텔레포트 실행
     * @return 성공 여부
     */
    public boolean teleport() {
        if (expired || cancelled) {
            return false;
        }

        if (!requester.isOnline() || !target.isOnline()) {
            return false;
        }

        requester.teleport(target.getLocation());
        requester.sendMessage(ChatColor.GREEN + target.getName() + "님에게 텔레포트했습니다!");
        target.sendMessage(ChatColor.GREEN + requester.getName() + "님이 텔레포트했습니다!");

        return true;
    }

    /**
     * 요청 만료
     * @param wasAccepted 수락되었는지 여부 (수락되었으면 만료 메시지 출력 안 함)
     */
    public void expire(boolean wasAccepted) {
        this.expired = true;
        
        // 수락되지 않았을 때만 만료 메시지 출력
        if (!wasAccepted) {
            if (requester.isOnline()) {
                requester.sendMessage(ChatColor.RED + target.getName() + "님에게 보낸 텔레포트 요청이 만료되었습니다.");
            }
            if (target.isOnline()) {
                target.sendMessage(ChatColor.RED + requester.getName() + "님의 텔레포트 요청이 만료되었습니다.");
            }
        }
    }
    
    /**
     * 요청 만료 (기존 호환성 유지)
     */
    public void expire() {
        expire(false);
    }

    /**
     * 요청 취소
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * 요청이 만료되었는지 확인
     * @return 만료 여부
     */
    public boolean isExpired() {
        return expired;
    }

    /**
     * 요청이 취소되었는지 확인
     * @return 취소 여부
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 요청자 조회
     * @return 요청자
     */
    public Player getRequester() {
        return requester;
    }

    /**
     * 대상 조회
     * @return 대상
     */
    public Player getTarget() {
        return target;
    }

    /**
     * 요청 생성 시간 조회
     * @return 생성 시간 (밀리초)
     */
    public long getCreatedTime() {
        return createdTime;
    }
}

