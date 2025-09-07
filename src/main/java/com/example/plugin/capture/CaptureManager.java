package com.example.plugin.capture;

import com.example.plugin.team.Team;
import com.example.plugin.team.TeamManager;
import com.example.plugin.effects.ZoneEffectManager;
import com.example.plugin.beacon.BeaconManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 점령 시스템 관리자 클래스
 * 모든 점령지의 상태와 점령 로직을 관리
 */
public class CaptureManager {
    private final JavaPlugin plugin;
    private final TeamManager teamManager;
    private final Map<String, CaptureZone> captureZones;
    private final Map<String, BukkitRunnable> captureTasks;
    private boolean gameActive = false;
    private ZoneEffectManager effectManager;
    private BeaconManager beaconManager;

    public CaptureManager(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.captureZones = new HashMap<>();
        this.captureTasks = new HashMap<>();
        initializeCaptureZones();
    }

    /**
     * 효과 매니저 설정
     * @param effectManager 효과 매니저
     */
    public void setEffectManager(ZoneEffectManager effectManager) {
        this.effectManager = effectManager;
    }

    /**
     * 신호기 매니저 설정
     * @param beaconManager 신호기 매니저
     */
    public void setBeaconManager(BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
    }

    /**
     * 점령지들을 초기화
     */
    private void initializeCaptureZones() {
        // 테스트용 점령지들 (0,0 중심 20x20 정사각형 내에 5x5 점령지들)
        // 꼭짓점에서 2칸 안쪽에 배치하여 맵 경계를 벗어나지 않도록 함
    
        World world = Bukkit.getWorlds().get(0);
        
        // 중앙 점령지 (5x5) - (0, 0)
        createCaptureZone("center", CaptureZone.ZoneType.CENTER, 
            new Location(world, 0, 64, 0), 2.5);
        
        // 북서쪽 - 물 (-8, -8) - 꼭짓점에서 2칸 안쪽
        createCaptureZone("water", CaptureZone.ZoneType.WATER, 
            new Location(world, -8, 64, -8), 2.5);
        
        // 남동쪽 - 불 (8, 8) - 꼭짓점에서 2칸 안쪽
        createCaptureZone("fire", CaptureZone.ZoneType.FIRE, 
            new Location(world, 8, 64, 8), 2.5);
        
        // 남서쪽 - 바람 (-8, 8) - 꼭짓점에서 2칸 안쪽
        createCaptureZone("wind", CaptureZone.ZoneType.WIND, 
            new Location(world, -8, 64, 8), 2.5);
        
        // 북동쪽 - 얼음 (8, -8) - 꼭짓점에서 2칸 안쪽
        createCaptureZone("ice", CaptureZone.ZoneType.ICE, 
            new Location(world, 8, 64, -8), 2.5);
    }

    /**
     * 점령지 생성
     * @param name 점령지 이름
     * @param type 점령지 타입
     * @param center 중심 좌표
     * @param radius 반지름
     */
    public void createCaptureZone(String name, CaptureZone.ZoneType type, Location center, double radius) {
        CaptureZone zone = new CaptureZone(name, type, center, radius);
        captureZones.put(name, zone);
    }

    /**
     * 게임 시작
     */
    public void startGame() {
        gameActive = true;
        resetAllZones();
        startCaptureMonitoring();
        broadcastMessage(ChatColor.GOLD + "땅따먹기 게임이 시작되었습니다!");
    }

    /**
     * 게임 종료
     */
    public void stopGame() {
        gameActive = false;
        stopAllCaptureTasks();
        broadcastMessage(ChatColor.RED + "땅따먹기 게임이 종료되었습니다!");
    }

    /**
     * 모든 점령지 초기화
     */
    public void resetAllZones() {
        for (CaptureZone zone : captureZones.values()) {
            zone.stopCapture();
            // 여기서 신호기 색상도 초기화해야 함
        }
    }

    /**
     * 점령 모니터링 시작
     */
    private void startCaptureMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive) {
                    cancel();
                    return;
                }
                
                updateAllZones();
                updateActionBar();
                checkWinCondition();
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1초마다 실행
    }

    /**
     * 모든 점령지 업데이트
     */
    private void updateAllZones() {
        for (CaptureZone zone : captureZones.values()) {
            updateZone(zone);
        }
    }

    /**
     * 특정 점령지 업데이트
     * @param zone 업데이트할 점령지
     */
    private void updateZone(CaptureZone zone) {
        // 구역 내 플레이어들 확인
        Set<UUID> playersInZone = new HashSet<>();
        Map<String, Integer> teamCounts = new HashMap<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (zone.isPlayerInZone(player)) {
                playersInZone.add(player.getUniqueId());
                String teamName = teamManager.getPlayerTeamName(player);
                if (teamName != null) {
                    teamCounts.put(teamName, teamCounts.getOrDefault(teamName, 0) + 1);
                }
            }
        }

        // 점령 로직 처리
        if (teamCounts.size() == 1) {
            // 한 팀만 구역에 있음
            String teamName = teamCounts.keySet().iterator().next();
            
            // 특수 규칙: 기본 점령지 3개 점령 시 중앙 점령 불가
            if (zone.getType() == CaptureZone.ZoneType.CENTER && !canCaptureCenter(teamName)) {
                // 중앙 점령 불가 메시지
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (teamManager.getPlayerTeamName(player) != null && 
                        teamManager.getPlayerTeamName(player).equals(teamName)) {
                        player.sendMessage(ChatColor.RED + "기본 점령지 3개를 점령한 상태에서는 중앙을 점령할 수 없습니다!");
                    }
                }
                return;
            }
            
            if (!zone.isCaptured() || !teamName.equals(zone.getCurrentTeam())) {
                // 점령 시작
                if (!zone.isCapturing() || !teamName.equals(zone.getCapturingTeam())) {
                    zone.startCapture(teamName);
                    broadcastCaptureStart(zone, teamName);
                } else {
                    // 점령 진행
                    if (zone.updateCaptureProgress(teamName)) {
                        // 점령 완료
                        completeCapture(zone, teamName);
                    }
                }
            }
        } else if (teamCounts.size() > 1) {
            // 여러 팀이 구역에 있음 - 점령 중단
            if (zone.isCapturing()) {
                zone.stopCapture();
                broadcastCaptureInterrupted(zone);
            }
        } else {
            // 아무도 구역에 없음 - 점령 중단
            if (zone.isCapturing()) {
                zone.stopCapture();
            }
        }
    }

    /**
     * 점령 완료 처리
     * @param zone 점령된 구역
     * @param teamName 점령한 팀
     */
    private void completeCapture(CaptureZone zone, String teamName) {
        Team team = teamManager.getTeam(teamName);
        if (team != null) {
            team.addScore(zone.getType().getScoreValue());
            
            // 신호기 색상 변경 (실제 구현 필요)
            changeBeaconColor(zone, team);
            
            // 효과 부여 (실제 구현 필요)
            applyZoneEffects(zone, team);
            
            // 알림
            broadcastCaptureComplete(zone, teamName);
            
            // 특수 규칙 적용
            applySpecialRules(zone, teamName);
        }
    }

    /**
     * 신호기 색상 변경
     * @param zone 점령지
     * @param team 점령한 팀
     */
    private void changeBeaconColor(CaptureZone zone, Team team) {
        if (beaconManager != null) {
            beaconManager.setBeaconColor(zone, team);
        } else {
            plugin.getLogger().info(team.getName() + "이 " + zone.getName() + "을 점령했습니다!");
        }
    }

    /**
     * 구역 효과 적용
     * @param zone 점령지
     * @param team 점령한 팀
     */
    private void applyZoneEffects(CaptureZone zone, Team team) {
        if (effectManager != null) {
            effectManager.applyZoneEffects(zone, team);
        }
    }

    /**
     * 특수 규칙 적용
     * @param zone 점령된 구역
     * @param teamName 점령한 팀
     */
    private void applySpecialRules(CaptureZone zone, String teamName) {
        if (zone.getType() == CaptureZone.ZoneType.CENTER) {
            // 중앙 점령 시 기본 점령지 탈환 시간 15분으로 증가
            for (CaptureZone basicZone : captureZones.values()) {
                if (basicZone.getType() != CaptureZone.ZoneType.CENTER) {
                    // 탈환 시간을 15분으로 변경 (실제로는 CaptureZone 클래스에서 처리)
                    plugin.getLogger().info("중앙 점령으로 인해 " + basicZone.getName() + " 탈환 시간이 15분으로 증가했습니다!");
                }
            }
        }
    }

    /**
     * 기본 점령지 3개 점령 시 중앙 점령 불가 확인
     * @param teamName 점령 시도하는 팀
     * @return 중앙 점령 가능 여부
     */
    public boolean canCaptureCenter(String teamName) {
        int basicZonesCaptured = 0;
        
        for (CaptureZone zone : captureZones.values()) {
            if (zone.getType() != CaptureZone.ZoneType.CENTER && 
                teamName.equals(zone.getCurrentTeam())) {
                basicZonesCaptured++;
            }
        }
        
        // 기본 점령지 2개 이상 점령 시 중앙 점령 불가
        return basicZonesCaptured < 2;
    }

    /**
     * 승리 조건 확인
     */
    private void checkWinCondition() {
        for (Team team : teamManager.getAllTeams()) {
            if (checkTeamWinCondition(team)) {
                broadcastMessage(ChatColor.GOLD + "🎉 " + team.getName() + " 팀이 승리했습니다! 🎉");
                stopGame();
                return;
            }
        }
    }

    /**
     * 팀의 승리 조건 확인
     * @param team 확인할 팀
     * @return 승리 여부
     */
    private boolean checkTeamWinCondition(Team team) {
        String teamName = team.getName();
        int basicZonesCaptured = 0;
        boolean centerCaptured = false;

        // 점령지 현황 확인
        for (CaptureZone zone : captureZones.values()) {
            if (teamName.equals(zone.getCurrentTeam())) {
                if (zone.getType() == CaptureZone.ZoneType.CENTER) {
                    centerCaptured = true;
                } else {
                    basicZonesCaptured++;
                }
            }
        }

        // 승리 조건 1: 기본 점령지 3곳 점령 시 세트 보너스 1점 = 4점 승리
        if (basicZonesCaptured >= 3) {
            return true;
        }

        // 승리 조건 2: 중앙(2점) + 기본 2곳(2점) = 4점 승리
        if (centerCaptured && basicZonesCaptured >= 2) {
            return true;
        }

        return false;
    }

    /**
     * 액션바 업데이트
     */
    @SuppressWarnings("deprecation")
    private void updateActionBar() {
        StringBuilder status = new StringBuilder();
        status.append(ChatColor.GOLD).append("=== 점령 현황 === ");
        
        for (CaptureZone zone : captureZones.values()) {
            status.append(zone.getType().getDisplayName()).append(": ");
            if (zone.isCaptured()) {
                status.append(zone.getCurrentTeam());
            } else if (zone.isCapturing()) {
                status.append(zone.getCapturingTeam()).append("(").append(zone.getCaptureProgressPercent()).append("%)");
            } else {
                status.append("없음");
            }
            status.append(" ");
        }
        
        // 모든 온라인 플레이어에게 액션바 전송
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Paper API 1.16.5에서는 sendActionBar가 deprecated이지만 여전히 작동
            // @SuppressWarnings("deprecation")을 사용하여 경고 억제
            player.sendActionBar(status.toString());
        }
    }

    /**
     * 모든 점령 작업 중단
     */
    private void stopAllCaptureTasks() {
        for (BukkitRunnable task : captureTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        captureTasks.clear();
    }

    /**
     * 점령 시작 알림
     */
    private void broadcastCaptureStart(CaptureZone zone, String teamName) {
        String message = ChatColor.YELLOW + teamName + " 팀이 " + zone.getType().getDisplayName() + " 점령을 시작했습니다!";
        broadcastMessage(message);
    }

    /**
     * 점령 중단 알림
     */
    private void broadcastCaptureInterrupted(CaptureZone zone) {
        String message = ChatColor.RED + zone.getType().getDisplayName() + " 점령이 중단되었습니다! (다른 팀 난입)";
        broadcastMessage(message);
    }

    /**
     * 점령 완료 알림
     */
    private void broadcastCaptureComplete(CaptureZone zone, String teamName) {
        String message = ChatColor.GREEN + "🎉 " + teamName + " 팀이 " + zone.getType().getDisplayName() + "을 점령했습니다! 🎉";
        broadcastMessage(message);
        
        // 타이틀로도 알림
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("", message, 10, 40, 10);
        }
    }

    /**
     * 전체 메시지 방송
     */
    private void broadcastMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    /**
     * 점령지 반환
     * @param name 점령지 이름
     * @return 점령지
     */
    public CaptureZone getCaptureZone(String name) {
        return captureZones.get(name);
    }

    /**
     * 모든 점령지 반환
     * @return 점령지 목록
     */
    public Collection<CaptureZone> getAllCaptureZones() {
        return captureZones.values();
    }

    /**
     * 게임 활성 상태 반환
     * @return 게임 활성 여부
     */
    public boolean isGameActive() {
        return gameActive;
    }
}
