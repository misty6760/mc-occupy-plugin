package com.example.plugin.commands;

import com.example.plugin.team.Team;
import com.example.plugin.team.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 팀 관련 명령어 처리
 * /team - 팀 목록 표시
 * /team <팀이름> - 특정 팀 정보 표시
 */
public class TeamCommand implements CommandExecutor, TabCompleter {
    private final TeamManager teamManager;

    public TeamCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // 팀 목록 표시
            showTeamList(player);
        } else if (args.length == 1) {
            // 특정 팀 정보 표시
            String teamName = args[0];
            showTeamInfo(player, teamName);
        } else {
            player.sendMessage(ChatColor.RED + "사용법: /team [팀이름]");
        }

        return true;
    }

    /**
     * 팀 목록 표시
     * @param player 대상 플레이어
     */
    private void showTeamList(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 팀 목록 ===");
        
        for (Team team : teamManager.getAllTeams()) {
            player.sendMessage(team.toString());
        }
        
        // 플레이어의 현재 팀 표시
        String currentTeam = teamManager.getPlayerTeamName(player);
        if (currentTeam != null) {
            player.sendMessage(ChatColor.GREEN + "현재 팀: " + currentTeam);
        } else {
            player.sendMessage(ChatColor.GRAY + "현재 팀: 없음");
        }
    }

    /**
     * 특정 팀 정보 표시
     * @param player 대상 플레이어
     * @param teamName 팀 이름
     */
    private void showTeamInfo(Player player, String teamName) {
        Team team = teamManager.getTeam(teamName);
        if (team == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 팀입니다!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== " + team.getName() + " 팀 정보 ===");
        player.sendMessage(ChatColor.YELLOW + "색상: " + team.getColor() + "■");
        player.sendMessage(ChatColor.YELLOW + "점수: " + team.getScore());
        player.sendMessage(ChatColor.YELLOW + "팀원 수: " + team.getMemberCount() + "/3");
        
        if (team.getMemberCount() > 0) {
            player.sendMessage(ChatColor.YELLOW + "팀원 수: " + team.getMemberCount() + "명");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 첫 번째 인수: 팀 이름들
            for (String teamName : teamManager.getTeamNames()) {
                if (teamName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(teamName);
                }
            }
        }
        
        return completions;
    }
}
