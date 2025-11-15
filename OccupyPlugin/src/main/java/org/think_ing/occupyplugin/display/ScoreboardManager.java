package org.think_ing.occupyplugin.display;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.think_ing.occupyplugin.OccupyPlugin;

import java.util.Map;

/**
 * 스코어보드 관리자
 * 사이드바에 팀별 점수를 표시합니다
 */
public class ScoreboardManager {
    
    private Objective objective;
    
    public ScoreboardManager(OccupyPlugin plugin) {
        // plugin 파라미터는 향후 확장을 위해 유지
    }
    
    /**
     * 스코어보드 생성 및 초기화
     */
    public void createScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        
        // 기존 objective 제거
        if (objective != null) {
            objective.unregister();
        }
        
        // 새로운 objective 생성
        @SuppressWarnings("deprecation")
        Objective newObjective = scoreboard.registerNewObjective(
                "occupy_score", 
                "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "점령전 점수"
        );
        objective = newObjective;
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    
    /**
     * 팀별 점수 업데이트
     * @param teamScores 팀별 점수 맵
     */
    public void updateScores(Map<Team, Integer> teamScores) {
        if (objective == null) {
            createScoreboard();
        }
        
        // 기존 점수 초기화
        for (String entry : objective.getScoreboard().getEntries()) {
            objective.getScoreboard().resetScores(entry);
        }
        
        // 팀별 점수 표시 (이름만 표시, 점수는 오른쪽 숫자로)
        for (Map.Entry<Team, Integer> entry : teamScores.entrySet()) {
            Team team = entry.getKey();
            int score = entry.getValue();
            
            @SuppressWarnings("deprecation")
            ChatColor teamColor = team.getColor();
            @SuppressWarnings("deprecation")
            String teamDisplayName = team.getDisplayName();
            
            // 팀 이름만 표시 (점수는 기본 스코어보드 숫자로)
            String displayText = teamColor + teamDisplayName;
            
            Score scoreEntry = objective.getScore(displayText);
            scoreEntry.setScore(score); // 오른쪽에 점수 숫자 표시
        }
    }
    
    /**
     * 모든 플레이어에게 스코어보드 표시
     */
    public void showToAllPlayers() {
        if (objective == null) {
            return;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(objective.getScoreboard());
        }
    }
    
    /**
     * 특정 플레이어에게 스코어보드 표시
     * @param player 플레이어
     */
    public void showToPlayer(Player player) {
        if (objective != null) {
            player.setScoreboard(objective.getScoreboard());
        }
    }
    
    /**
     * 스코어보드 제거
     */
    public void removeScoreboard() {
        if (objective != null) {
            objective.unregister();
            objective = null;
        }
        
        // 모든 플레이어를 메인 스코어보드로 되돌림
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mainScoreboard);
        }
    }
}

