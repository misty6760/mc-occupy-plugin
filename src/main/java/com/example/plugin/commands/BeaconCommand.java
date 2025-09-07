package com.example.plugin.commands;

import com.example.plugin.beacon.BeaconManager;
import com.example.plugin.capture.CaptureManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 신호기 관련 명령어 처리
 * /beacon create - 모든 점령지에 신호기 구조 생성
 * /beacon reset - 모든 신호기 색상 초기화
 */
public class BeaconCommand implements CommandExecutor, TabCompleter {
    private final BeaconManager beaconManager;
    private final CaptureManager captureManager;

    public BeaconCommand(BeaconManager beaconManager, CaptureManager captureManager) {
        this.beaconManager = beaconManager;
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
            showBeaconHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                createBeaconStructures(player);
                break;
            case "reset":
                resetBeaconColors(player);
                break;
            case "help":
                showBeaconHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다!");
                showBeaconHelp(player);
                break;
        }

        return true;
    }

    /**
     * 신호기 구조 생성
     * @param player 명령어 실행자
     */
    private void createBeaconStructures(Player player) {
        if (!player.hasPermission("landcapture.admin")) {
            player.sendMessage(ChatColor.RED + "이 명령어는 관리자만 사용할 수 있습니다!");
            return;
        }

        beaconManager.createAllBeaconStructures(captureManager.getAllCaptureZones(), player);
        player.sendMessage(ChatColor.GREEN + "✅ 모든 점령지에 신호기 구조를 생성했습니다!");
        player.sendMessage(ChatColor.YELLOW + "각 점령지 중심에 신호기가 배치되었습니다. (Y=" + (player.getLocation().getBlockY() - 1) + ")");
    }

    /**
     * 신호기 색상 초기화
     * @param player 명령어 실행자
     */
    private void resetBeaconColors(Player player) {
        if (!player.hasPermission("landcapture.admin")) {
            player.sendMessage(ChatColor.RED + "이 명령어는 관리자만 사용할 수 있습니다!");
            return;
        }

        beaconManager.resetAllBeaconColors();
        player.sendMessage(ChatColor.GREEN + "✅ 모든 신호기 색상을 초기화했습니다!");
    }

    /**
     * 신호기 명령어 도움말 표시
     * @param player 대상 플레이어
     */
    private void showBeaconHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 신호기 명령어 도움말 ===");
        player.sendMessage(ChatColor.YELLOW + "/beacon create - 모든 점령지에 신호기 구조 생성");
        player.sendMessage(ChatColor.YELLOW + "/beacon reset - 모든 신호기 색상 초기화");
        player.sendMessage(ChatColor.YELLOW + "/beacon help - 이 도움말 표시");
        player.sendMessage(ChatColor.GRAY + "");
        player.sendMessage(ChatColor.GRAY + "※ 신호기 구조는 각 점령지 중심 아래에 생성됩니다.");
        player.sendMessage(ChatColor.GRAY + "※ 점령 시 신호기 위에 팀 색상의 색유리가 배치됩니다.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            if ("create".startsWith(input)) {
                completions.add("create");
            }
            if ("reset".startsWith(input)) {
                completions.add("reset");
            }
            if ("help".startsWith(input)) {
                completions.add("help");
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}
