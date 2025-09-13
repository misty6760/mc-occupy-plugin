package com.example.plugin.commands;

import com.example.plugin.capture.CaptureManager;
import com.example.plugin.capture.CaptureZone;
import com.example.plugin.team.Team;
import com.example.plugin.team.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 정보 관련 명령어 처리
 * /info - 전체 게임 정보 표시
 * /info team - 팀 정보 표시
 * /info capture - 점령지 정보 표시
 */
public class InfoCommand implements CommandExecutor, TabCompleter {
    private final TeamManager teamManager;
    private final CaptureManager captureManager;

    public InfoCommand(TeamManager teamManager, CaptureManager captureManager) {
        this.teamManager = teamManager;
        this.captureManager = captureManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // 전체 정보 표시
            showAllInfo(player);
        } else if (args.length == 1) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "team":
                    showTeamInfo(player);
                    break;
                case "capture":
                    showCaptureInfo(player);
                    break;
                case "game":
                    showGameInfo(player);
                    break;
                case "help":
                    showInfoHelp(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다!");
                    showInfoHelp(player);
                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "사용법: /info [team|capture|game|help]");
        }

        return true;
    }

    /**
     * 전체 정보 표시
     * @param player 대상 플레이어
     */
    private void showAllInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 땅따먹기 게임 정보 ===");
        
        // 게임 상태
        if (captureManager.isGameActive()) {
            player.sendMessage(ChatColor.GREEN + "게임 상태: 진행 중");
        } else {
            player.sendMessage(ChatColor.RED + "게임 상태: 대기 중");
        }
        
        player.sendMessage("");
        
        // 팀 정보
        showTeamInfo(player);
        player.sendMessage("");
        
        // 점령지 정보
        showCaptureInfo(player);
    }

    /**
     * 팀 정보 표시
     * @param player 대상 플레이어
     */
    private void showTeamInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 팀 현황 ===");
        
        for (Team team : teamManager.getAllTeams()) {
            player.sendMessage(team.toString());
        }
        
        // 플레이어의 현재 팀
        String currentTeam = teamManager.getPlayerTeamName(player);
        if (currentTeam != null) {
            player.sendMessage(ChatColor.GREEN + "내 팀: " + currentTeam);
        } else {
            player.sendMessage(ChatColor.GRAY + "내 팀: 없음");
        }
    }

    /**
     * 점령지 정보 표시
     * @param player 대상 플레이어
     */
    private void showCaptureInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 점령지 현황 ===");
        
        for (CaptureZone zone : captureManager.getAllCaptureZones()) {
            String status;
            if (zone.isCaptured()) {
                status = ChatColor.GREEN + "점령됨 (" + zone.getCurrentTeam() + ")";
            } else if (zone.isCapturing()) {
                status = ChatColor.YELLOW + "점령 중 (" + zone.getCapturingTeam() + 
                        " - " + zone.getCaptureProgressPercent() + "%)";
            } else {
                status = ChatColor.RED + "점령되지 않음";
            }
            
            player.sendMessage(ChatColor.YELLOW + zone.getType().getDisplayName() + ": " + status);
        }
    }

    /**
     * 게임 정보 표시
     * @param player 대상 플레이어
     */
    private void showGameInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 게임 정보 ===");
        
        if (captureManager.isGameActive()) {
            player.sendMessage(ChatColor.GREEN + "게임이 진행 중입니다!");
            
            // 승리 조건
            player.sendMessage(ChatColor.YELLOW + "승리 조건:");
            player.sendMessage(ChatColor.GRAY + "• 기본 점령지 3곳 점령 → 세트 보너스 1점 = 4점 승리");
            player.sendMessage(ChatColor.GRAY + "• 중앙(2점) + 기본 2곳(2점) = 4점 승리");
            
            // 특수 규칙
            player.sendMessage(ChatColor.YELLOW + "특수 규칙:");
            player.sendMessage(ChatColor.GRAY + "• 기본 점령지 2곳 점령 시 중앙 점령 불가");
            player.sendMessage(ChatColor.GRAY + "• 중앙 점령 시 기본 점령지 탈환 시간 15분으로 증가");
        } else {
            player.sendMessage(ChatColor.RED + "게임이 진행 중이 아닙니다.");
            player.sendMessage(ChatColor.YELLOW + "게임을 시작하려면 /game start 명령어를 사용하세요.");
        }
    }

    /**
     * 정보 명령어 도움말 표시
     * @param player 대상 플레이어
     */
    private void showInfoHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 정보 명령어 도움말 ===");
        player.sendMessage(ChatColor.YELLOW + "/info - 전체 게임 정보 표시");
        player.sendMessage(ChatColor.YELLOW + "/info team - 팀 정보 표시");
        player.sendMessage(ChatColor.YELLOW + "/info capture - 점령지 정보 표시");
        player.sendMessage(ChatColor.YELLOW + "/info game - 게임 규칙 및 상태 표시");
        player.sendMessage(ChatColor.YELLOW + "/info help - 이 도움말 표시");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 첫 번째 인수: info 하위 명령어
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            if ("team".startsWith(input)) {
                completions.add("team");
            }
            if ("capture".startsWith(input)) {
                completions.add("capture");
            }
            if ("game".startsWith(input)) {
                completions.add("game");
            }
            if ("help".startsWith(input)) {
                completions.add("help");
            }
            
            return completions;
        }
        
        return Collections.emptyList();
    }
}
