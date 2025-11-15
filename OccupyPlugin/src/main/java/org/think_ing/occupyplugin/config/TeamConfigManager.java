package org.think_ing.occupyplugin.config;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.think_ing.occupyplugin.OccupyPlugin;

import java.util.List;

/**
 * 팀 설정 관리자
 * 참여 팀 목록을 config.yml에 추가/제거합니다
 */
public class TeamConfigManager {
    
    private final OccupyPlugin plugin;
    
    public TeamConfigManager(OccupyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 참여 팀 추가
     * @param teamName 팀 이름
     * @param player 명령을 실행한 플레이어 (메시지 전송용)
     * @return 추가 성공 여부
     */
    public boolean addTeam(String teamName, Player player) {
        List<String> teams = plugin.getConfig().getStringList("participating_teams");
        
        if (teams.contains(teamName)) {
            return false; // 이미 존재함
        }
        
        teams.add(teamName);
        plugin.getConfig().set("participating_teams", teams);
        plugin.saveConfig();
        plugin.getGameManager().loadConfig();
        
        player.sendMessage(ChatColor.GREEN + "[OccupyPlugin] " + ChatColor.WHITE 
                + "팀 '" + teamName + "'이(가) 점령전 참여팀 목록에 자동으로 추가되었습니다.");
        
        return true;
    }
    
    /**
     * 참여 팀 제거
     * @param teamName 팀 이름
     * @param player 명령을 실행한 플레이어 (메시지 전송용)
     * @return 제거 성공 여부
     */
    public boolean removeTeam(String teamName, Player player) {
        List<String> teams = plugin.getConfig().getStringList("participating_teams");
        
        if (!teams.contains(teamName)) {
            return false; // 존재하지 않음
        }
        
        teams.remove(teamName);
        plugin.getConfig().set("participating_teams", teams);
        plugin.saveConfig();
        plugin.getGameManager().loadConfig();
        
        player.sendMessage(ChatColor.GREEN + "[OccupyPlugin] " + ChatColor.WHITE 
                + "팀 '" + teamName + "'이(가) 점령전 참여팀 목록에서 자동으로 제거되었습니다.");
        
        return true;
    }
}

