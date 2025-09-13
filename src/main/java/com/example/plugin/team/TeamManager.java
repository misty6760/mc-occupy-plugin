package com.example.plugin.team;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.*;

/**
 * 팀 관리자 클래스
 * 팀 생성, 삭제, 플레이어 배치 등을 관리
 */
public class TeamManager {
    private final Map<String, Team> teams;
    private final Map<UUID, String> playerTeams; // 플레이어 UUID -> 팀 이름

    public TeamManager() {
        this.teams = new HashMap<>();
        this.playerTeams = new HashMap<>();
        initializeTeams();
    }

    /**
     * 기본 팀들을 초기화
     */
    private void initializeTeams() {
        // 4개 팀 생성
        createTeam("빨강팀", ChatColor.RED);
        createTeam("파랑팀", ChatColor.BLUE);
        createTeam("초록팀", ChatColor.GREEN);
        createTeam("노랑팀", ChatColor.YELLOW);
    }

    /**
     * 새 팀 생성
     * @param name 팀 이름
     * @param color 팀 색상
     * @return 생성된 팀
     */
    public Team createTeam(String name, ChatColor color) {
        Team team = new Team(name, color);
        teams.put(name, team);
        return team;
    }

    /**
     * 팀 이름 변경
     * @param oldName 기존 팀 이름
     * @param newName 새로운 팀 이름
     * @return 변경 성공 여부
     */
    public boolean renameTeam(String oldName, String newName) {
        Team team = teams.get(oldName);
        if (team == null || teams.containsKey(newName)) {
            return false; // 기존 팀이 없거나 새 이름이 이미 존재
        }
        
        // 팀 이름 변경
        team.setName(newName);
        teams.remove(oldName);
        teams.put(newName, team);
        
        // 플레이어 팀 매핑 업데이트
        for (UUID memberId : team.getMembers()) {
            playerTeams.put(memberId, newName);
        }
        
        return true;
    }

    /**
     * 팀 삭제
     * @param name 팀 이름
     * @return 삭제 성공 여부
     */
    public boolean removeTeam(String name) {
        Team team = teams.remove(name);
        if (team != null) {
            // 팀원들을 모두 팀에서 제거
            for (UUID memberId : team.getMembers()) {
                playerTeams.remove(memberId);
            }
            return true;
        }
        return false;
    }

    /**
     * 플레이어를 팀에 배치
     * @param player 배치할 플레이어
     * @param teamName 팀 이름
     * @return 배치 성공 여부
     */
    public boolean assignPlayerToTeam(Player player, String teamName) {
        Team team = teams.get(teamName);
        if (team == null) {
            return false;
        }

        // 기존 팀에서 제거
        removePlayerFromTeam(player);

        // 새 팀에 추가
        if (team.addMember(player)) {
            playerTeams.put(player.getUniqueId(), teamName);
            return true;
        }
        return false;
    }

    /**
     * 플레이어를 팀에서 제거
     * @param player 제거할 플레이어
     * @return 제거 성공 여부
     */
    public boolean removePlayerFromTeam(Player player) {
        String teamName = playerTeams.remove(player.getUniqueId());
        if (teamName != null) {
            Team team = teams.get(teamName);
            if (team != null) {
                team.removeMember(player);
                return true;
            }
        }
        return false;
    }

    /**
     * 플레이어의 팀 반환
     * @param player 플레이어
     * @return 플레이어가 속한 팀
     */
    public Team getPlayerTeam(Player player) {
        String teamName = playerTeams.get(player.getUniqueId());
        return teamName != null ? teams.get(teamName) : null;
    }

    /**
     * 플레이어의 팀 이름 반환
     * @param player 플레이어
     * @return 팀 이름
     */
    public String getPlayerTeamName(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    /**
     * 팀 반환
     * @param name 팀 이름
     * @return 팀
     */
    public Team getTeam(String name) {
        return teams.get(name);
    }

    /**
     * 모든 팀 반환
     * @return 팀 목록
     */
    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    /**
     * 팀 목록 반환
     * @return 팀 이름 목록
     */
    public Set<String> getTeamNames() {
        return teams.keySet();
    }

    /**
     * 플레이어가 팀에 속해있는지 확인
     * @param player 플레이어
     * @return 팀 소속 여부
     */
    public boolean hasTeam(Player player) {
        return playerTeams.containsKey(player.getUniqueId());
    }

    /**
     * 팀원 수가 가장 적은 팀 반환
     * @return 팀원 수가 가장 적은 팀
     */
    public Team getTeamWithLeastMembers() {
        return teams.values().stream()
                .min(Comparator.comparingInt(Team::getMemberCount))
                .orElse(null);
    }

    /**
     * 팀원 수가 가장 많은 팀 반환
     * @return 팀원 수가 가장 많은 팀
     */
    public Team getTeamWithMostMembers() {
        return teams.values().stream()
                .max(Comparator.comparingInt(Team::getMemberCount))
                .orElse(null);
    }

    /**
     * 모든 팀의 점수 초기화
     */
    public void resetAllScores() {
        for (Team team : teams.values()) {
            team.setScore(0);
        }
    }

    /**
     * 승리 팀 반환 (4점 이상)
     * @return 승리 팀, 없으면 null
     */
    public Team getWinningTeam() {
        return teams.values().stream()
                .filter(team -> team.getScore() >= 4)
                .findFirst()
                .orElse(null);
    }

    /**
     * 팀 현황을 문자열로 반환
     * @return 팀 현황 문자열
     */
    public String getTeamStatus() {
        StringBuilder status = new StringBuilder();
        status.append(ChatColor.GOLD).append("=== 팀 현황 ===\n");
        
        for (Team team : teams.values()) {
            status.append(team.toString()).append("\n");
        }
        
        return status.toString();
    }
}
