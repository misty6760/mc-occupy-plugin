package org.think_ing.occupyplugin.tpa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * 팀 검증 관리자
 * 플레이어들이 같은 팀인지 확인합니다
 */
public class TeamValidator {

    /**
     * 두 플레이어가 같은 팀인지 확인
     * @param player1 플레이어 1
     * @param player2 플레이어 2
     * @return 같은 팀이면 true
     */
    public boolean areSameTeam(Player player1, Player player2) {
        Team team1 = getPlayerTeam(player1);
        Team team2 = getPlayerTeam(player2);

        // 둘 다 팀에 속해있고 같은 팀인지 확인
        return team1 != null && team2 != null && team1.equals(team2);
    }

    /**
     * 플레이어의 팀 조회
     * @param player 플레이어
     * @return 팀 (없으면 null)
     */
    public Team getPlayerTeam(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        return scoreboard.getEntryTeam(player.getName());
    }

    /**
     * 플레이어가 팀에 속해있는지 확인
     * @param player 플레이어
     * @return 팀에 속해있으면 true
     */
    public boolean hasTeam(Player player) {
        return getPlayerTeam(player) != null;
    }
}

