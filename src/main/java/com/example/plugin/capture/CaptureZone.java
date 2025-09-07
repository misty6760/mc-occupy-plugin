package com.example.plugin.capture;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.*;

/**
 * 점령 구역 클래스
 * 각 점령지의 정보와 상태를 관리
 */
public class CaptureZone {
    public enum ZoneType {
        WATER("물", 1, 5, 10),      // 기본 점령지
        FIRE("불", 1, 5, 10),
        ICE("얼음", 1, 5, 10),
        WIND("바람", 1, 5, 10),
        CENTER("중앙", 2, 10, 15);   // 특수 점령지

        private final String displayName;
        private final int scoreValue;
        private final int captureTime;
        private final int recaptureTime;

        ZoneType(String displayName, int scoreValue, int captureTime, int recaptureTime) {
            this.displayName = displayName;
            this.scoreValue = scoreValue;
            this.captureTime = captureTime;
            this.recaptureTime = recaptureTime;
        }

        public String getDisplayName() { return displayName; }
        public int getScoreValue() { return scoreValue; }
        public int getCaptureTime() { return captureTime; }
        public int getRecaptureTime() { return recaptureTime; }
    }

    private final String name;
    private final ZoneType type;
    private final Location center;
    private final double radius;
    private final Set<UUID> playersInZone;
    private String currentTeam;
    private int captureProgress;
    private int maxProgress;
    private boolean isCapturing;
    private String capturingTeam;

    public CaptureZone(String name, ZoneType type, Location center, double radius) {
        this.name = name;
        this.type = type;
        this.center = center;
        this.radius = radius;
        this.playersInZone = new HashSet<>();
        this.currentTeam = null;
        this.captureProgress = 0;
        this.maxProgress = type.getCaptureTime() * 60; // 분을 초로 변환
        this.isCapturing = false;
        this.capturingTeam = null;
    }

    /**
     * 플레이어가 구역에 들어왔는지 확인
     * @param player 확인할 플레이어
     * @return 구역 내부 여부
     */
    public boolean isPlayerInZone(Player player) {
        if (center == null || player.getWorld() != center.getWorld()) {
            return false;
        }

        Location playerLoc = player.getLocation();
        double distance = center.distance(playerLoc);
        return distance <= radius;
    }

    /**
     * 플레이어를 구역에 추가
     * @param player 추가할 플레이어
     */
    public void addPlayer(Player player) {
        playersInZone.add(player.getUniqueId());
    }

    /**
     * 플레이어를 구역에서 제거
     * @param player 제거할 플레이어
     */
    public void removePlayer(Player player) {
        playersInZone.remove(player.getUniqueId());
    }

    /**
     * 구역 내 플레이어 수 반환
     * @return 구역 내 플레이어 수
     */
    public int getPlayerCount() {
        return playersInZone.size();
    }

    /**
     * 구역 내 플레이어 UUID 목록 반환
     * @return 플레이어 UUID 목록
     */
    public Set<UUID> getPlayersInZone() {
        return new HashSet<>(playersInZone);
    }

    /**
     * 점령 진행률 업데이트
     * @param teamName 점령 시도하는 팀
     * @return 점령 완료 여부
     */
    public boolean updateCaptureProgress(String teamName) {
        if (isCapturing && capturingTeam.equals(teamName)) {
            captureProgress++;
            
            if (captureProgress >= maxProgress) {
                // 점령 완료
                completeCapture(teamName);
                return true;
            }
        }
        return false;
    }

    /**
     * 점령 시작
     * @param teamName 점령 시도하는 팀
     */
    public void startCapture(String teamName) {
        if (isCapturing && !capturingTeam.equals(teamName)) {
            // 다른 팀이 점령 중이면 중단
            stopCapture();
        }
        
        if (!isCapturing) {
            isCapturing = true;
            capturingTeam = teamName;
            captureProgress = 0;
        }
    }

    /**
     * 점령 중단
     */
    public void stopCapture() {
        isCapturing = false;
        capturingTeam = null;
        captureProgress = 0;
    }

    /**
     * 점령 완료
     * @param teamName 점령한 팀
     */
    private void completeCapture(String teamName) {
        currentTeam = teamName;
        isCapturing = false;
        capturingTeam = null;
        captureProgress = 0;
    }

    /**
     * 점령 진행률 반환 (0.0 ~ 1.0)
     * @return 점령 진행률
     */
    public double getCaptureProgress() {
        return (double) captureProgress / maxProgress;
    }

    /**
     * 점령 진행률 퍼센트 반환
     * @return 점령 진행률 퍼센트
     */
    public int getCaptureProgressPercent() {
        return (int) (getCaptureProgress() * 100);
    }

    /**
     * 점령 중인지 확인
     * @return 점령 중 여부
     */
    public boolean isCapturing() {
        return isCapturing;
    }

    /**
     * 점령 시도 중인 팀 반환
     * @return 점령 시도 중인 팀
     */
    public String getCapturingTeam() {
        return capturingTeam;
    }

    /**
     * 현재 점령 팀 반환
     * @return 현재 점령 팀
     */
    public String getCurrentTeam() {
        return currentTeam;
    }

    /**
     * 점령지 이름 반환
     * @return 점령지 이름
     */
    public String getName() {
        return name;
    }

    /**
     * 점령지 타입 반환
     * @return 점령지 타입
     */
    public ZoneType getType() {
        return type;
    }

    /**
     * 중심 좌표 반환
     * @return 중심 좌표
     */
    public Location getCenter() {
        return center;
    }

    /**
     * 반지름 반환
     * @return 반지름
     */
    public double getRadius() {
        return radius;
    }

    /**
     * 점령지가 점령되어 있는지 확인
     * @return 점령 여부
     */
    public boolean isCaptured() {
        return currentTeam != null;
    }

    /**
     * 점령지 정보를 문자열로 반환
     * @return 점령지 정보 문자열
     */
    @Override
    public String toString() {
        StringBuilder info = new StringBuilder();
        info.append(type.getDisplayName()).append(" (").append(name).append(")");
        
        if (isCaptured()) {
            info.append(" - 점령됨: ").append(currentTeam);
        } else if (isCapturing) {
            info.append(" - 점령 중: ").append(capturingTeam).append(" (").append(getCaptureProgressPercent()).append("%)");
        } else {
            info.append(" - 점령되지 않음");
        }
        
        return info.toString();
    }
}
