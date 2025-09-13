package com.example.plugin.commands;

import com.example.plugin.team.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 팀 탈퇴 명령어 처리
 * /leave - 현재 팀에서 탈퇴
 */
public class LeaveCommand implements CommandExecutor, TabCompleter {
    private final TeamManager teamManager;

    public LeaveCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            player.sendMessage(ChatColor.RED + "사용법: /leave");
            return true;
        }

        // 팀에 속해있는지 확인
        if (!teamManager.hasTeam(player)) {
            player.sendMessage(ChatColor.RED + "현재 팀에 속해있지 않습니다!");
            return true;
        }

        String currentTeam = teamManager.getPlayerTeamName(player);
        
        // 팀에서 탈퇴
        if (teamManager.removePlayerFromTeam(player)) {
            player.sendMessage(ChatColor.GREEN + currentTeam + " 팀에서 탈퇴했습니다!");
        } else {
            player.sendMessage(ChatColor.RED + "팀 탈퇴에 실패했습니다!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // /leave 명령어는 인수가 없으므로 빈 리스트 반환
        return Collections.emptyList();
    }
}
