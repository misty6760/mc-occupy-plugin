package org.think_ing.occupyplugin.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.think_ing.occupyplugin.config.TeamConfigManager;

/**
 * 팀 명령어 리스너
 * /team 명령어를 감지하여 자동으로 참여 팀 목록을 관리합니다
 */
public class TeamListener implements Listener {

    private final TeamConfigManager teamConfigManager;

    public TeamListener(TeamConfigManager teamConfigManager) {
        this.teamConfigManager = teamConfigManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        String[] args = message.split(" ");

        // /team add <teamname> 명령어 감지
        if (isTeamAddCommand(args)) {
            String teamName = args[2];
            teamConfigManager.addTeam(teamName, event.getPlayer());
        }

        // /team remove <teamname> 명령어 감지
        if (isTeamRemoveCommand(args)) {
            String teamName = args[2];
            teamConfigManager.removeTeam(teamName, event.getPlayer());
        }
    }
    
    /**
     * /team add 명령어 확인
     */
    private boolean isTeamAddCommand(String[] args) {
        return args.length >= 3 
                && args[0].equals("/team") 
                && args[1].equals("add");
    }
    
    /**
     * /team remove 명령어 확인
     */
    private boolean isTeamRemoveCommand(String[] args) {
        return args.length == 3 
                && args[0].equals("/team") 
                && args[1].equals("remove");
    }
}
