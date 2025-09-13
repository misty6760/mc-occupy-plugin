package com.example.plugin.capture;

import com.example.plugin.team.Team;
import com.example.plugin.team.TeamManager;
import com.example.plugin.effects.ZoneEffectManager;
import com.example.plugin.beacon.BeaconManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
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
    private File zonesFile;
    private FileConfiguration zonesConfig;
    private BossBar centerBossBar;
    
    // 테스트용 점령 시간 설정
    private int testCaptureTime = 300; // 기본 5분 (300초)
    private boolean useTestTime = false;

    public CaptureManager(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.captureZones = new HashMap<>();
        this.captureTasks = new HashMap<>();
        this.centerBossBar = Bukkit.createBossBar("중앙 점령지", BarColor.YELLOW, BarStyle.SOLID);
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
     * 설정 파일에서 점령지 정보를 로드
     */
    private void initializeCaptureZones() {
        loadZonesConfig();
        
        if (zonesConfig == null) {
            plugin.getLogger().warning("점령지 설정 파일을 로드할 수 없습니다. 기본 설정을 사용합니다.");
            initializeDefaultZones();
            return;
        }
        
        // 설정 파일에서 점령지 정보 로드
        for (String zoneName : zonesConfig.getConfigurationSection("zones").getKeys(false)) {
            String path = "zones." + zoneName;
            
            String worldName = zonesConfig.getString(path + ".world", "world");
            int x = zonesConfig.getInt(path + ".x", 0);
            int y = zonesConfig.getInt(path + ".y", 64);
            int z = zonesConfig.getInt(path + ".z", 0);
            double radius = zonesConfig.getDouble(path + ".radius", 2.5);
            String typeStr = zonesConfig.getString(path + ".type", "CENTER");
            
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("월드 '" + worldName + "'을 찾을 수 없습니다. 기본 월드를 사용합니다.");
                world = Bukkit.getWorlds().get(0);
            }
            
            CaptureZone.ZoneType type;
            try {
                type = CaptureZone.ZoneType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("잘못된 점령지 타입: " + typeStr + ". CENTER로 설정합니다.");
                type = CaptureZone.ZoneType.CENTER;
            }
            
            createCaptureZone(zoneName, type, new Location(world, x, y, z), radius);
            plugin.getLogger().info("점령지 로드: " + zoneName + " (" + typeStr + ") at " + x + ", " + y + ", " + z);
        }
    }
    
    /**
     * 기본 점령지 설정 (설정 파일이 없을 때 사용)
     */
    private void initializeDefaultZones() {
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
     * 점령지 설정 파일 로드
     */
    private void loadZonesConfig() {
        zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        
        if (!zonesFile.exists()) {
            plugin.saveResource("zones.yml", false);
        }
        
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
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
        
        // 보스바 정리
        if (centerBossBar != null) {
            centerBossBar.removeAll();
        }
        
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
        // captureZones.values()를 한 번만 호출하여 메모리 사용량 최적화
        Collection<CaptureZone> zones = captureZones.values();
        for (CaptureZone zone : zones) {
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
                String capturingTeam = zone.getCapturingTeam();
                zone.stopCapture();
                broadcastCaptureInterrupted(zone);
                
                // 점령 중이던 팀에게 "점령지 뺏기는 중" 메시지 전송
                broadcastZoneBeingCaptured(zone, capturingTeam);
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
            // 메모리 효율성을 위해 한 번만 반복
            Collection<CaptureZone> zones = captureZones.values();
            for (CaptureZone basicZone : zones) {
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
        
        // 기본 점령지 3개 이상 점령 시 중앙 점령 불가
        return basicZonesCaptured < 3;
    }

    /**
     * 승리 조건 확인
     */
    private void checkWinCondition() {
        for (Team team : teamManager.getAllTeams()) {
            if (checkTeamWinCondition(team)) {
                broadcastMessage(ChatColor.GOLD + team.getName() + " 팀이 승리했습니다!");
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
     * 액션바 및 보스바 업데이트
     */
    @SuppressWarnings("deprecation")
    private void updateActionBar() {
        // 각 플레이어별로 액션바 표시 (기본 점령지 점령 상태만)
        for (Player player : Bukkit.getOnlinePlayers()) {
            String teamName = teamManager.getPlayerTeamName(player);
            if (teamName == null) continue;
            
            boolean inZone = false;
            
            // 기본 점령지들만 액션바에 표시 (플레이어가 있는 점령지)
            for (CaptureZone zone : captureZones.values()) {
                if (zone.getType() == CaptureZone.ZoneType.CENTER) continue; // 중앙은 제외
                
                if (zone.isPlayerInZone(player)) {
                    inZone = true;
                    // 점령지 내 팀 수 확인
                    int teamCount = getTeamCountInZone(zone);
                    
                    
                    if (zone.isCaptured()) {
                        // 점령된 상태
                        if (teamName.equals(zone.getCurrentTeam())) {
                            player.sendActionBar(ChatColor.GREEN + zone.getType().getDisplayName() + ": 점령됨!");
                        } else {
                            player.sendActionBar(ChatColor.RED + zone.getType().getDisplayName() + ": " + zone.getCurrentTeam() + " 점령");
                        }
                    } else if (zone.isCapturing()) {
                        if (teamCount == 1) {
                            // 하나의 팀만 있을 때 (고착 상태 X) - 초록색으로 퍼센트 및 남은 시간 표시
                            if (teamName.equals(zone.getCapturingTeam())) {
                                player.sendActionBar(ChatColor.GREEN + zone.getType().getDisplayName() + ": " + zone.getCaptureProgressPercent()
                                      + "% (" + zone.getRemainingCaptureTimeFormatted() + ")");
                            } else {
                                player.sendActionBar(ChatColor.GRAY + zone.getType().getDisplayName() + ": " + zone.getCapturingTeam() + " 점령 중");
                            }
                        } else {
                            // 2개 이상의 팀이 있을 때 (고착 상태 O) - 노란색으로 퍼센트 표시, 시간 정지
                            if (teamName.equals(zone.getCapturingTeam())) {
                                player.sendActionBar(ChatColor.YELLOW + zone.getType().getDisplayName() + ": " + zone.getCaptureProgressPercent() + "% (정지)");
                            } else {
                                player.sendActionBar(ChatColor.GRAY + zone.getType().getDisplayName() + ": " + zone.getCapturingTeam() + " 점령 중 (정지)");
                            }
                        }
                    } else {
                        player.sendActionBar(ChatColor.WHITE + zone.getType().getDisplayName() + ": 미점령");
                    }
                    break; // 하나의 점령지에만 있을 수 있으므로 첫 번째 점령지만 표시
                }
            }
            
            // 점령지에 들어가지 않은 플레이어는 액션바를 비움
            if (!inZone) {
                player.sendActionBar("");
            }
        }
        
        // 중앙 점령지는 모든 플레이어에게 보스바로 표시
        updateCenterZoneBossBar();
        
        // 스코어보드 업데이트
        updateScoreboard();
    }
    
    /**
     * 점령지 내 팀 수 확인
     */
    private int getTeamCountInZone(CaptureZone zone) {
        Set<String> teamsInZone = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (zone.isPlayerInZone(player)) {
                String teamName = teamManager.getPlayerTeamName(player);
                if (teamName != null) {
                    teamsInZone.add(teamName);
                }
            }
        }
        return teamsInZone.size();
    }
    
    /**
     * 중앙 점령지 보스바 업데이트
     */
    private void updateCenterZoneBossBar() {
        CaptureZone centerZone = captureZones.get("center");
        if (centerZone == null) return;
        
        StringBuilder bossBarText = new StringBuilder();
        bossBarText.append("중앙 점령지: ");
        
        if (centerZone.isCaptured()) {
            bossBarText.append(centerZone.getCurrentTeam()).append(" 점령");
            centerBossBar.setColor(BarColor.GREEN);
        } else if (centerZone.isCapturing()) {
            bossBarText.append(centerZone.getCapturingTeam())
                      .append(" 점령 중 (").append(centerZone.getCaptureProgressPercent()).append("%) - ")
                      .append(centerZone.getRemainingCaptureTimeFormatted());
            centerBossBar.setColor(BarColor.YELLOW);
            centerBossBar.setProgress(centerZone.getCaptureProgress());
        } else {
            bossBarText.append("미점령");
            centerBossBar.setColor(BarColor.WHITE);
            centerBossBar.setProgress(0.0);
        }
        
        centerBossBar.setTitle(bossBarText.toString());
        
        // 모든 플레이어에게 보스바 표시
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!centerBossBar.getPlayers().contains(player)) {
                centerBossBar.addPlayer(player);
            }
        }
    }
    
    /**
     * 스코어보드 업데이트
     */
    private void updateScoreboard() {
        // 스코어보드는 현재 구현하지 않음 (액션바 충돌 방지)
        // 추후 사이드바 스코어보드로 구현 예정
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
     * 테스트용 점령 시간 설정
     * @param time 점령 시간 (초)
     */
    public void setTestCaptureTime(int time) {
        this.testCaptureTime = time;
        this.useTestTime = true;
        
        // 모든 점령지에 테스트 시간 적용
        for (CaptureZone zone : captureZones.values()) {
            zone.setCaptureTime(time);
        }
    }
    
    /**
     * 테스트용 점령 시간 비활성화 (원래 시간으로 복구)
     */
    public void resetTestCaptureTime() {
        this.useTestTime = false;
        
        // 모든 점령지를 원래 시간으로 복구
        for (CaptureZone zone : captureZones.values()) {
            zone.setCaptureTime(zone.getType().getCaptureTime() * 60);
        }
    }
    
    /**
     * 현재 테스트용 점령 시간 반환
     * @return 테스트용 점령 시간 (초)
     */
    public int getTestCaptureTime() {
        return testCaptureTime;
    }
    
    /**
     * 테스트 모드 사용 여부 확인
     * @return 테스트 모드 사용 중이면 true
     */
    public boolean isTestMode() {
        return useTestTime;
    }
    
    /**
     * 실제 사용할 점령 시간 반환
     * @return 테스트 모드면 테스트 시간, 아니면 기본 시간
     */
    public int getActualCaptureTime() {
        return useTestTime ? testCaptureTime : 300; // 기본 5분
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
     * 점령지 뺏기는 중 알림
     */
    private void broadcastZoneBeingCaptured(CaptureZone zone, String teamName) {
        String message = ChatColor.RED + "경고! " + zone.getType().getDisplayName() + "이 다른 팀에게 뺏기고 있습니다!";
        
        // 점령 중이던 팀에게만 메시지 전송
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerTeam = teamManager.getPlayerTeamName(player);
            if (teamName.equals(playerTeam)) {
                player.sendMessage(message);
                player.sendTitle(ChatColor.RED + "점령지 뺏기는 중!", 
                               ChatColor.RED + zone.getType().getDisplayName() + "을 지키세요!", 10, 60, 10);
            }
        }
    }

    /**
     * 점령 완료 알림
     */
    private void broadcastCaptureComplete(CaptureZone zone, String teamName) {
        // 중앙 점령지만 모든 플레이어에게 알림
        if (zone.getType() == CaptureZone.ZoneType.CENTER) {
            String message = ChatColor.GREEN + teamName + " 팀이 " + zone.getType().getDisplayName() + "을 점령했습니다!";
            broadcastMessage(message);
            
            // 타이틀로도 알림
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle("", message, 10, 40, 10);
            }
        }
        
        // 점령한 팀에게만 특별한 타이틀 표시
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerTeam = teamManager.getPlayerTeamName(player);
            if (teamName.equals(playerTeam)) {
                String zoneName = zone.getType().getDisplayName();
                player.sendTitle(ChatColor.GREEN + zoneName + "지역 점령 완료!", 
                               ChatColor.GREEN + "축하합니다!", 10, 60, 10);
            }
        }
        
        // 기존 점령 팀에게만 알림 (기존 점령 팀이 있었다면)
        String previousTeam = zone.getCurrentTeam();
        if (previousTeam != null && !previousTeam.equals(teamName)) {
            String lostMessage = ChatColor.RED + "경고! " + zone.getType().getDisplayName() + "이 " + teamName + " 팀에게 점령되었습니다!";
            
            // 기존 점령 팀에게만 빨간색 알림
            for (Player player : Bukkit.getOnlinePlayers()) {
                String playerTeam = teamManager.getPlayerTeamName(player);
                if (previousTeam.equals(playerTeam)) {
                    player.sendMessage(lostMessage);
                    player.sendTitle(ChatColor.RED + "점령지 점령됨!", lostMessage, 10, 60, 10);
                }
            }
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
