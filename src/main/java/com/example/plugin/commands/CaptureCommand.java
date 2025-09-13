package com.example.plugin.commands;

import com.example.plugin.capture.CaptureManager;
import com.example.plugin.capture.CaptureZone;
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
 * 점령 관련 명령어 처리
 * /capture - 현재 위치의 점령지 정보 표시
 * /capture <점령지이름> - 특정 점령지 정보 표시
 */
public class CaptureCommand implements CommandExecutor, TabCompleter {
    private final CaptureManager captureManager;

    public CaptureCommand(CaptureManager captureManager) {
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
            // 현재 위치의 점령지 정보 표시
            showCurrentZoneInfo(player);
        } else if (args.length == 1) {
            // 특정 점령지 정보 표시
            String zoneName = args[0];
            showZoneInfo(player, zoneName);
        } else {
            player.sendMessage(ChatColor.RED + "사용법: /capture [점령지이름]");
        }

        return true;
    }

    /**
     * 현재 위치의 점령지 정보 표시
     * @param player 대상 플레이어
     */
    private void showCurrentZoneInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 현재 위치 점령지 정보 ===");
        
        boolean inZone = false;
        for (CaptureZone zone : captureManager.getAllCaptureZones()) {
            if (zone.isPlayerInZone(player)) {
                inZone = true;
                showZoneDetails(player, zone);
                break;
            }
        }
        
        if (!inZone) {
            player.sendMessage(ChatColor.GRAY + "현재 위치는 점령지가 아닙니다.");
            player.sendMessage(ChatColor.YELLOW + "점령지 목록:");
            for (CaptureZone zone : captureManager.getAllCaptureZones()) {
                player.sendMessage(ChatColor.GRAY + "- " + zone.getType().getDisplayName() + 
                                 " (" + zone.getName() + ")");
            }
        }
    }

    /**
     * 특정 점령지 정보 표시
     * @param player 대상 플레이어
     * @param zoneName 점령지 이름
     */
    private void showZoneInfo(Player player, String zoneName) {
        CaptureZone zone = captureManager.getCaptureZone(zoneName);
        if (zone == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 점령지입니다!");
            player.sendMessage(ChatColor.YELLOW + "사용 가능한 점령지:");
            for (CaptureZone z : captureManager.getAllCaptureZones()) {
                player.sendMessage(ChatColor.GRAY + "- " + z.getName() + " (" + z.getType().getDisplayName() + ")");
            }
            return;
        }

        showZoneDetails(player, zone);
    }

    /**
     * 점령지 상세 정보 표시
     * @param player 대상 플레이어
     * @param zone 점령지
     */
    private void showZoneDetails(Player player, CaptureZone zone) {
        player.sendMessage(ChatColor.YELLOW + "점령지: " + zone.getType().getDisplayName());
        player.sendMessage(ChatColor.YELLOW + "이름: " + zone.getName());
        player.sendMessage(ChatColor.YELLOW + "중심 좌표: " + 
                          zone.getCenter().getBlockX() + ", " + 
                          zone.getCenter().getBlockY() + ", " + 
                          zone.getCenter().getBlockZ());
        player.sendMessage(ChatColor.YELLOW + "반지름: " + zone.getRadius() + "블록");
        player.sendMessage(ChatColor.YELLOW + "점수 가치: " + zone.getType().getScoreValue() + "점");
        
        if (zone.isCaptured()) {
            player.sendMessage(ChatColor.GREEN + "상태: 점령됨 (" + zone.getCurrentTeam() + ")");
        } else if (zone.isCapturing()) {
            player.sendMessage(ChatColor.YELLOW + "상태: 점령 중 (" + zone.getCapturingTeam() + 
                             " - " + zone.getCaptureProgressPercent() + "%)");
        } else {
            player.sendMessage(ChatColor.RED + "상태: 점령되지 않음");
        }
        
        player.sendMessage(ChatColor.YELLOW + "구역 내 플레이어 수: " + zone.getPlayerCount());
        
        // 점령 시간 정보
        if (zone.getType() == CaptureZone.ZoneType.CENTER) {
            player.sendMessage(ChatColor.GRAY + "점령 시간: " + zone.getType().getCaptureTime() + "분");
            player.sendMessage(ChatColor.GRAY + "탈환 시간: " + zone.getType().getRecaptureTime() + "분");
        } else {
            player.sendMessage(ChatColor.GRAY + "점령 시간: " + zone.getType().getCaptureTime() + "분");
            player.sendMessage(ChatColor.GRAY + "탈환 시간: " + zone.getType().getRecaptureTime() + "분");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 첫 번째 인수: 점령지 이름들 또는 명령어
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            // 명령어들 먼저 추가
            if ("list".startsWith(input)) {
                completions.add("list");
            }
            if ("stop".startsWith(input)) {
                completions.add("stop");
            }
            
            // 점령지 이름들
            for (CaptureZone zone : captureManager.getAllCaptureZones()) {
                String zoneName = zone.getName();
                if (zoneName.toLowerCase().startsWith(input)) {
                    completions.add(zoneName);
                }
            }
            
            return completions;
        }
        
        return Collections.emptyList();
    }
}
