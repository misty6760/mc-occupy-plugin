package org.think_ing.occupyplugin.game;

import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 점수 관리자
 * 팀별 점수를 계산하고 관리합니다
 * 
 * 점수 규칙:
 * - 기본 점령지: 각 1점
 * - 중앙 점령지: 2점
 * - 기본 점령지 4개 모두 점령: +1점 (세트 보너스)
 * - 승리 조건: 5점 이상
 */
public class ScoreManager {
    
    private final String centerPointName;
    
    public ScoreManager(String centerPointName) {
        this.centerPointName = centerPointName;
    }
    
    /**
     * 모든 팀의 점수 계산
     * @param occupationPoints 점령지 리스트
     * @param participatingTeams 참여 팀 리스트
     * @return 팀별 점수 맵
     */
    public Map<Team, Integer> calculateScores(List<OccupationPoint> occupationPoints, List<Team> participatingTeams) {
        Map<Team, Integer> scores = new HashMap<>();
        
        for (Team team : participatingTeams) {
            if (team != null) {
                int score = calculateTeamScore(occupationPoints, team);
                scores.put(team, score);
            }
        }
        
        return scores;
    }
    
    /**
     * 특정 팀의 점수 계산
     * @param occupationPoints 점령지 리스트
     * @param team 팀
     * @return 팀 점수
     */
    public int calculateTeamScore(List<OccupationPoint> occupationPoints, Team team) {
        int score = 0;
        int basePointsOwned = 0;
        int totalBasePoints = 0;
        boolean ownsCenter = false;
        
        // 점령지 점수 계산 (완전히 점령한 경우에만 점수 부여)
        for (OccupationPoint point : occupationPoints) {
            // 점령 완료 여부 확인 (owner가 설정되어 있고, 점령 진행도가 완료되어야 함)
            if (point.getOwner() == null || !point.getOwner().equals(team)) {
                // 점령하지 않은 점령지
                if (!point.getName().equals(centerPointName)) {
                    totalBasePoints++;
                }
                continue;
            }
            
            // 점령 완료 확인: owner가 설정되어 있고, 점령 진행도가 0이어야 함 (점령 완료 시 진행도 초기화됨)
            // 또는 점령 진행도가 목표 시간 이상이어야 함
            boolean isFullyCaptured = point.getOwner() != null && point.getOwner().equals(team);
            
            if (!isFullyCaptured) {
                // 점령 진행 중이면 점수 부여 안 함
                if (!point.getName().equals(centerPointName)) {
                    totalBasePoints++;
                }
                continue;
            }
            
            // 팀이 완전히 점령한 점령지
            if (point.getName().equals(centerPointName)) {
                // 중앙 점령지: 완전히 점령했을 때만 2점
                score += 2;
                ownsCenter = true;
            } else {
                // 기본 점령지: 1점
                score += 1;
                basePointsOwned++;
                totalBasePoints++;
            }
        }
        
        // 세트 보너스 1: 기본 점령지 4개 모두 점령 시 +1점
        if (totalBasePoints == 4 && basePointsOwned == 4) {
            score += 1;
        }
        
        // 세트 보너스 2: 기본 점령지 2개 + 중앙 점령 완료 시 +1점
        // 보너스는 기본 2개(2점) + 중앙 완전 점령(2점) = 4점일 때 +1점
        if (basePointsOwned >= 2 && ownsCenter) {
            score += 1;
        }
        
        return score;
    }
    
    /**
     * 승리 팀 확인 (5점 이상)
     * @param scores 팀별 점수 맵
     * @return 승리 팀 (없으면 null)
     */
    public Team getWinningTeam(Map<Team, Integer> scores) {
        for (Map.Entry<Team, Integer> entry : scores.entrySet()) {
            if (entry.getValue() >= 5) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 특정 팀의 점수 조회
     * @param scores 팀별 점수 맵
     * @param team 팀
     * @return 점수
     */
    public int getTeamScore(Map<Team, Integer> scores, Team team) {
        return scores.getOrDefault(team, 0);
    }
}

