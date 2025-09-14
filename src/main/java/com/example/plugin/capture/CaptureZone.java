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
        WATER("아틀란스", 1, 5, 10),      // 기본 점령지
        FIRE("이그니스", 1, 5, 10),
        ICE("크라이오시스", 1, 5, 10),
        WIND("사이클론즈", 1, 5, 10),
        CENTER("제네시스", 2, 10, 15);   // 특수 점령지

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
    private long lastCaptureTime; // 마지막 점령 시간 (밀리초)
    private boolean isRecaptureProtected; // 재탈환 보호 상태
    
    // 테스트용 재탈환 시간 설정
    private int testRecaptureTime = 10; // 기본 10분
    private boolean useTestRecaptureTime = false;

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
        this.lastCaptureTime = 0;
        this.isRecaptureProtected = false;
    }
    
    /**
     * 점령 시간 설정 (테스트용)
     * @param time 점령 시간 (초)
     */
    public void setCaptureTime(int time) {
        this.maxProgress = time;
    }
    
    /**
     * 재탈환 시간 설정 (테스트용)
     * @param time 재탈환 시간 (분)
     */
    public void setRecaptureTime(int time) {
        // ZoneType의 recaptureTime은 final이므로 별도 필드로 관리
        this.testRecaptureTime = time;
        this.useTestRecaptureTime = true;
    }
    
    /**
     * 재탈환 시간 원래대로 복구
     */
    public void resetRecaptureTime() {
        this.useTestRecaptureTime = false;
    }
    
    /**
     * 실제 재탈환 시간 반환 (테스트 모드면 테스트 시간, 아니면 기본 시간)
     * @return 재탈환 시간 (분)
     */
    public int getActualRecaptureTime() {
        return useTestRecaptureTime ? testRecaptureTime : type.getRecaptureTime();
    }

    /**
     * 플레이어가 구역에 들어왔는지 확인 (정사각형 영역)
     * @param player 확인할 플레이어
     * @return 구역 내부 여부
     */
    public boolean isPlayerInZone(Player player) {
        if (center == null || player.getWorld() != center.getWorld()) {
            return false;
        }

        Location playerLoc = player.getLocation();
        
        // 정사각형 영역 계산 (반지름 대신 절반 크기 사용)
        double halfSize = radius;
        double dx = Math.abs(playerLoc.getX() - center.getX());
        double dz = Math.abs(playerLoc.getZ() - center.getZ());
        
        return dx <= halfSize && dz <= halfSize;
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
     * 구역 내 플레이어 UUID 목록 반환 (읽기 전용)
     * @return 플레이어 UUID 목록
     */
    public Set<UUID> getPlayersInZone() {
        return Collections.unmodifiableSet(playersInZone);
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
     * @return 점령 시작 가능 여부
     */
    public boolean startCapture(String teamName) {
        // 재탈환 보호 중이면 점령 불가
        if (isRecaptureProtected()) {
            return false;
        }
        
        if (isCapturing && !capturingTeam.equals(teamName)) {
            // 다른 팀이 점령 중이면 중단
            stopCapture();
        }
        
        if (!isCapturing) {
            isCapturing = true;
            capturingTeam = teamName;
            captureProgress = 0;
        }
        
        return true;
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
     * 남은 점령 시간 반환 (초)
     * @return 남은 점령 시간
     */
    public int getRemainingCaptureTime() {
        if (!isCapturing) return 0;
        return maxProgress - captureProgress;
    }

    /**
     * 남은 점령 시간을 분:초 형식으로 반환
     * @return 남은 점령 시간 (MM:SS)
     */
    public String getRemainingCaptureTimeFormatted() {
        int remaining = getRemainingCaptureTime();
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        return String.format("%d:%02d", minutes, seconds);
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
    
    /**
     * 점령지 초기화
     * 점령 상태, 점령 진행도, 점령 팀을 모두 초기화
     */
    public void resetZone() {
        this.currentTeam = null;
        this.captureProgress = 0;
        this.isCapturing = false;
        this.capturingTeam = null;
        this.playersInZone.clear();
        this.lastCaptureTime = 0;
        this.isRecaptureProtected = false;
    }
    
    /**
     * 재탈환 보호 상태 확인
     * @return 재탈환 보호 중이면 true
     */
    public boolean isRecaptureProtected() {
        if (!isRecaptureProtected) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long recaptureTimeMs = getActualRecaptureTime() * 60 * 1000; // 분을 밀리초로 변환
        
        // 재탈환 시간이 지났으면 보호 해제
        if (currentTime - lastCaptureTime >= recaptureTimeMs) {
            isRecaptureProtected = false;
            return false;
        }
        
        return true;
    }
    
    /**
     * 재탈환 보호 시간 반환 (초)
     * @return 남은 재탈환 보호 시간
     */
    public int getRemainingRecaptureProtectionTime() {
        if (!isRecaptureProtected) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long recaptureTimeMs = getActualRecaptureTime() * 60 * 1000; // 분을 밀리초로 변환
        long remainingMs = recaptureTimeMs - (currentTime - lastCaptureTime);
        
        return Math.max(0, (int) (remainingMs / 1000));
    }
    
    /**
     * 점령 완료 시 호출 (재탈환 보호 시작)
     */
    public void onCaptureComplete() {
        this.lastCaptureTime = System.currentTimeMillis();
        this.isRecaptureProtected = true;
    }
    
    /**
     * 재탈환 보호 강제 해제 (중앙 점령 시 기본 점령지 보호 시간 연장)
     */
    public void extendRecaptureProtection() {
        this.lastCaptureTime = System.currentTimeMillis();
        this.isRecaptureProtected = true;
    }
}
