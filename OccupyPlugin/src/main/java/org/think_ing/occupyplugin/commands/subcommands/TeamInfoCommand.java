package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.OccupyPlugin;
import org.think_ing.occupyplugin.commands.SubCommand;
import org.think_ing.occupyplugin.game.GameManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 팀 정보 조회 명령어
 */
public class TeamInfoCommand implements SubCommand {
    
    private final OccupyPlugin plugin;
    private final GameManager gameManager;
    
    public TeamInfoCommand(OccupyPlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Player target = null;
        
        if (args.length >= 2) {
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "플레이어 '" + args[1] + "'을(를) 찾을 수 없습니다.");
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        }
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "정보를 확인할 플레이어를 지정해주세요.");
            return true;
        }
        
        displayTeamInfo(sender, target);
        
        return true;
    }
    
    private void displayTeamInfo(CommandSender sender, Player target) {
        sender.sendMessage(ChatColor.YELLOW + "--- " + target.getName() + "님의 팀 정보 ---");
        
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getEntryTeam(target.getName());
        
        if (team == null) {
            sender.sendMessage(ChatColor.GRAY + "소속된 마인크래프트 팀이 없습니다.");
        } else {
            @SuppressWarnings("deprecation")
            ChatColor teamColor = team.getColor();
            sender.sendMessage(ChatColor.WHITE + "마인크래프트 팀: " + teamColor + team.getName());
            
            List<String> participatingTeams = gameManager.getParticipatingTeamNames();
            boolean isParticipating = participatingTeams.contains(team.getName());
            
            if (isParticipating) {
                sender.sendMessage(ChatColor.GREEN + "이 팀은 점령전에 참여하는 팀입니다.");
            } else {
                sender.sendMessage(ChatColor.RED + "이 팀은 점령전에 참여하지 않는 팀입니다.");
                sender.sendMessage(ChatColor.RED + "config.yml의 participating_teams 목록에 팀 이름을 정확히 추가해야 합니다.");
            }
        }
        
        sender.sendMessage(ChatColor.YELLOW + "----------------------------------");
        List<String> participatingTeams = gameManager.getParticipatingTeamNames();
        String teamList = participatingTeams.isEmpty() ? "없음" : String.join(", ", participatingTeams);
        sender.sendMessage(ChatColor.GRAY + "(config.yml에 등록된 참여팀 목록: " + teamList + ")");
    }
    
    @Override
    public String getName() {
        return "teaminfo";
    }
    
    @Override
    public String getDescription() {
        return "플레이어의 팀 정보를 표시합니다";
    }
    
    @Override
    public String getUsage() {
        return "/occupy teaminfo [플레이어]";
    }
    
    @Override
    public List<String> getTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

