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
 * 팀 가입 명령어 처리
 * /join <팀이름> - 팀에 가입
 */
public class JoinCommand implements CommandExecutor, TabCompleter {
    private final TeamManager teamManager;

    public JoinCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "사용법: /join <팀이름>");
            player.sendMessage(ChatColor.YELLOW + "사용 가능한 팀: " + String.join(", ", teamManager.getTeamNames()));
            return true;
        }

        String teamName = args[0];
        Team team = teamManager.getTeam(teamName);

        if (team == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 팀입니다!");
            player.sendMessage(ChatColor.YELLOW + "사용 가능한 팀: " + String.join(", ", teamManager.getTeamNames()));
            return true;
        }

        // 이미 팀에 속해있는지 확인
        if (teamManager.hasTeam(player)) {
            String currentTeam = teamManager.getPlayerTeamName(player);
            if (currentTeam.equals(teamName)) {
                player.sendMessage(ChatColor.YELLOW + "이미 " + teamName + " 팀에 속해있습니다!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "이미 " + currentTeam + " 팀에 속해있습니다!");
                player.sendMessage(ChatColor.YELLOW + "팀을 변경하려면 먼저 /leave 명령어를 사용하세요.");
                return true;
            }
        }

        // 팀이 가득 찼는지 확인
        if (team.isFull()) {
            player.sendMessage(ChatColor.RED + teamName + " 팀이 가득 찼습니다!");
            return true;
        }

        // 팀에 가입
        if (teamManager.assignPlayerToTeam(player, teamName)) {
            player.sendMessage(ChatColor.GREEN + "✅ " + teamName + " 팀에 가입했습니다!");
            player.sendMessage(ChatColor.YELLOW + "팀 색상: " + team.getColor() + "■");
            
            // 팀원들에게 알림
            player.sendMessage(ChatColor.GRAY + "팀원 수: " + team.getMemberCount() + "/3");
        } else {
            player.sendMessage(ChatColor.RED + "팀 가입에 실패했습니다!");
        }

        return true;
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
