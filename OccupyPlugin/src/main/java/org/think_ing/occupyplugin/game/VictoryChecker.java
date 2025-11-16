package org.think_ing.occupyplugin.game;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.think_ing.occupyplugin.OccupyPlugin;
import org.think_ing.occupyplugin.display.NotificationManager;

import java.util.List;

/**
 * 승리 조건 확인
 * 게임의 승리 조건을 확인하고 승리 팀을 결정합니다
 */
public class VictoryChecker {
    
    private final OccupyPlugin plugin;
    private final NotificationManager notificationManager;
    private final String centerPointName;
    
    public VictoryChecker(OccupyPlugin plugin, NotificationManager notificationManager, String centerPointName) {
        this.plugin = plugin;
        this.notificationManager = notificationManager;
        this.centerPointName = centerPointName;
    }
    
    /**
     * 승리 조건 확인
     * @param occupationPoints 점령지 리스트
     * @param participatingTeamNames 참여 팀 이름 리스트
     * @param onVictory 승리 시 실행할 콜백
     */
    public void checkVictory(List<OccupationPoint> occupationPoints, List<String> participatingTeamNames, 
                            VictoryCallback onVictory) {
        if (participatingTeamNames == null || participatingTeamNames.isEmpty()) {
            return;
        }
        
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        
        for (String teamName : participatingTeamNames) {
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                continue;
            }
            
            // 기본 점령지 점령 수 확인
            long ownedBasePoints = countOwnedBasePoints(occupationPoints, team);
            
            // 중앙 점령지 점령 확인
            boolean ownsCenter = checkOwnsCenter(occupationPoints, team);
            
            // 전체 기본 점령지 수
            long totalBasePoints = countTotalBasePoints(occupationPoints);
            
            // 승리 조건 1: 기본 점령지 4개 모두 점령 시 보너스 1점으로 5점 획득
            if (totalBasePoints > 0 && ownedBasePoints == totalBasePoints && totalBasePoints == 4) {
                endGame(team, "모든 기본 점령지를 점령했습니다! (보너스 +1점)", onVictory);
                return;
            }
            
            // 승리 조건 2: 기본 점령지 2개를 먼저 점령 후 중앙 점령지를 점령하면 4점 + 보너스 1점 = 5점
            if (ownedBasePoints >= 2 && ownsCenter) {
                endGame(team, "기본 점령지 2개와 중앙을 점령했습니다! (보너스 +1점)", onVictory);
                return;
            }
        }
    }
    
    /**
     * 팀이 점령한 기본 점령지 수 계산
     */
    private long countOwnedBasePoints(List<OccupationPoint> occupationPoints, Team team) {
        return occupationPoints.stream()
                .filter(p -> !p.getName().equals(centerPointName) && team.equals(p.getOwner()))
                .count();
    }
    
    /**
     * 팀이 중앙을 점령했는지 확인
     */
    private boolean checkOwnsCenter(List<OccupationPoint> occupationPoints, Team team) {
        return occupationPoints.stream()
                .anyMatch(p -> p.getName().equals(centerPointName) && team.equals(p.getOwner()));
    }
    
    /**
     * 전체 기본 점령지 수 계산
     */
    private long countTotalBasePoints(List<OccupationPoint> occupationPoints) {
        return occupationPoints.stream()
                .filter(p -> !p.getName().equals(centerPointName))
                .count();
    }
    
    /**
     * 게임 종료
     */
    private void endGame(Team winningTeam, String reason, VictoryCallback onVictory) {
        @SuppressWarnings("deprecation")
        ChatColor teamColor = winningTeam.getColor();
        @SuppressWarnings("deprecation")
        String teamDisplayName = winningTeam.getDisplayName();
        
        notificationManager.broadcastTitle(
                teamColor + teamDisplayName + " 팀 승리!",
                reason,
                10, 100, 20
        );
        
        onVictory.onVictory();
    }
    
    /**
     * 승리 콜백 인터페이스
     */
    @FunctionalInterface
    public interface VictoryCallback {
        void onVictory();
    }
}

