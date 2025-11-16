package org.think_ing.occupyplugin.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.think_ing.occupyplugin.OccupyPlugin;
import org.think_ing.occupyplugin.display.BossBarManager;
import org.think_ing.occupyplugin.display.NotificationManager;
import org.think_ing.occupyplugin.display.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 게임 관리자
 * 게임의 전체적인 흐름을 조율합니다
 */
public class GameManager {

    private final OccupyPlugin plugin;
    private final ConfigLoader configLoader;
    private final CaptureSystem captureSystem;
    private final EffectManager effectManager;
    private final ScoreManager scoreManager;
    private final BossBarManager bossBarManager;
    private final ScoreboardManager scoreboardManager;
    private final NotificationManager notificationManager;
    
    private List<OccupationPoint> occupationPoints = new ArrayList<>();
    private List<String> participatingTeamNames;
    private String centerPointName;
    private boolean isGameRunning = false;
    private BukkitRunnable gameTask;

    public GameManager(OccupyPlugin plugin) {
        this.plugin = plugin;
        this.configLoader = new ConfigLoader(plugin);
        this.notificationManager = new NotificationManager();
        this.bossBarManager = new BossBarManager(plugin);
        this.scoreboardManager = new ScoreboardManager(plugin);
        
        // 초기 설정 로드 (centerPointName 설정)
        loadConfig();
        
        this.captureSystem = new CaptureSystem(plugin, notificationManager, centerPointName);
        this.effectManager = new EffectManager(captureSystem);
        this.scoreManager = new ScoreManager(centerPointName);
    }
    
    /**
     * 설정 파일 로드
     */
    public void loadConfig() {
        occupationPoints = configLoader.loadOccupationPoints();
        participatingTeamNames = configLoader.loadParticipatingTeams();
        centerPointName = configLoader.loadCenterPointName();
                }
    
    /**
     * 게임 시작
     */
    public void startGame() {
        if (isGameRunning) {
            return;
        }
        
        isGameRunning = true;
        resetGame();
        bossBarManager.createCenterBossBar();
        scoreboardManager.createScoreboard();
        scoreboardManager.showToAllPlayers();

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateOccupationPoints();
                effectManager.updatePlayerEffects(occupationPoints);
                updateBossBars();
                updateScoreboard();
                checkVictory();
            }
        };
        
        gameTask.runTaskTimer(plugin, 0L, 20L);
        notificationManager.broadcastTitle(ChatColor.GREEN + "점령전 시작!", 
                "승리 조건: 5점 획득", 10, 70, 20);
    }

    /**
     * 테스트 모드로 게임 시작
     * 기본 점령지: 1분 (60초), 중앙 점령지: 2분 (120초)
     */
    public void startTestGame() {
        if (isGameRunning) {
            return;
        }
        
        // 테스트 모드 활성화
        for (OccupationPoint point : occupationPoints) {
            if (point.getName().equals(centerPointName)) {
                // 중앙 점령지: 2분
                point.setTestMode(true, 120, 120);
            } else {
                // 기본 점령지: 1분
                point.setTestMode(true, 60, 60);
            }
        }
        
        // 게임 시작
        startGame();
        notificationManager.broadcastMessage(ChatColor.YELLOW + "[테스트 모드] 점령 시간이 단축되었습니다!");
        notificationManager.broadcastMessage(ChatColor.GRAY + "기본 점령지: 1분, 중앙 점령지: 2분");
    }
    
    /**
     * 게임 종료
     */
    public void stopGame() {
        if (!isGameRunning) {
            return;
        }
        
        isGameRunning = false;
        
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        
        bossBarManager.hideCenterBossBar();
        scoreboardManager.removeScoreboard();
        
        // 테스트 모드 비활성화
        for (OccupationPoint point : occupationPoints) {
            point.setTestMode(false, 0, 0);
        }
        
        resetGame();
    }

    /**
     * 게임 초기화
     */
    private void resetGame() {
        captureSystem.resetPoints(occupationPoints);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBarManager.hideCaptureBossBar(player);
        }
    }

    /**
     * 점령지 업데이트
     */
    private void updateOccupationPoints() {
        captureSystem.updateOccupationPoints(occupationPoints, participatingTeamNames);
    }
    
    /**
     * 스코어보드 업데이트
     */
    private void updateScoreboard() {
        List<Team> teams = getParticipatingTeams();
        Map<Team, Integer> scores = scoreManager.calculateScores(occupationPoints, teams);
        scoreboardManager.updateScores(scores);
    }
    
    /**
     * 승리 조건 확인
     */
    private void checkVictory() {
        List<Team> teams = getParticipatingTeams();
        Map<Team, Integer> scores = scoreManager.calculateScores(occupationPoints, teams);
        Team winningTeam = scoreManager.getWinningTeam(scores);

        if (winningTeam != null) {
            int finalScore = scores.get(winningTeam);
            @SuppressWarnings("deprecation")
            ChatColor teamColor = winningTeam.getColor();
            @SuppressWarnings("deprecation")
            String teamDisplayName = winningTeam.getDisplayName();

        notificationManager.broadcastTitle(
                    teamColor + teamDisplayName + " 팀 승리!",
                    "최종 점수: " + finalScore + "점",
                    10, 100, 20
            );
            
            stopGame();
        }
    }

    /**
     * 참여 팀 객체 리스트 조회
     */
    private List<Team> getParticipatingTeams() {
        List<Team> teams = new ArrayList<>();
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        for (String teamName : participatingTeamNames) {
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                teams.add(team);
            }
        }
        
        return teams;
                    }
    
    /**
     * 보스바 업데이트
     */
    private void updateBossBars() {
        OccupationPoint centerPoint = getCenterPoint();
        if (centerPoint != null) {
            bossBarManager.updateCenterBossBar(centerPoint);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            // 중앙 점령지에 있는 경우 개별 보스바 숨김
            if (centerPoint != null && captureSystem.isPlayerInPoint(player, centerPoint)) {
                bossBarManager.hideCaptureBossBar(player);
                continue;
            }

            // 기본 점령지의 보스바 표시
            updatePlayerCaptureBossBar(player);
        }
    }
    
    /**
     * 플레이어의 개별 보스바 업데이트
     */
    private void updatePlayerCaptureBossBar(Player player) {
            boolean inAnyZone = false;
        
            for (OccupationPoint point : occupationPoints) {
            // 중앙 점령지는 건너뜀
                if (point.getName().equals(centerPointName)) {
                    continue;
                }

            // 점령 진행 중인 점령지에 있고, 자신의 팀이 점령하려는 중이 아닌 경우
            if (captureSystem.isPlayerInPoint(player, point) && point.getCaptureProgress() > 0) {
                Team playerTeam = captureSystem.getPlayerTeam(player);
                if (playerTeam != null && !playerTeam.equals(point.getOwner())) {
                        bossBarManager.showCaptureBossBar(player, point);
                        inAnyZone = true;
                        break;
                    }
                }
            }
        
            if (!inAnyZone) {
                bossBarManager.hideCaptureBossBar(player);
        }
    }

    /**
     * 중앙 점령지 조회
     */
    private OccupationPoint getCenterPoint() {
        for (OccupationPoint point : occupationPoints) {
            if (point.getName().equals(centerPointName)) {
                return point;
            }
        }
        return null;
    }

    /**
     * 플레이어의 팀 조회 (외부 접근용)
     */
    public Team getPlayerTeam(Player player) {
        return captureSystem.getPlayerTeam(player);
    }
    
    /**
     * 게임 실행 중 여부 확인
     */
    public boolean isGameRunning() {
        return isGameRunning;
    }

    /**
     * 참여 팀 목록 조회
     */
    public List<String> getParticipatingTeamNames() {
        return participatingTeamNames != null ? participatingTeamNames : new ArrayList<>();
    }
}
