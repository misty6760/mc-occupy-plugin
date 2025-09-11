package com.example.plugin.commands;

import com.example.plugin.capture.CaptureManager;
import com.example.plugin.capture.CaptureZone;
import com.example.plugin.beacon.BeaconManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 점령지 설정 명령어 클래스
 * 점령지 위치를 동적으로 설정할 수 있음
 */
public class ZoneCommand implements CommandExecutor, TabCompleter {
    private final CaptureManager captureManager;
    private final BeaconManager beaconManager;
    private final File zonesFile;
    private FileConfiguration zonesConfig;

    public ZoneCommand(CaptureManager captureManager, BeaconManager beaconManager, File zonesFile) {
        this.captureManager = captureManager;
        this.beaconManager = beaconManager;
        this.zonesFile = zonesFile;
        this.zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("landcapture.admin")) {
            player.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        if (args.length == 0) {
            showZoneHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "사용법: /zone set <점령지이름>");
                    return true;
                }
                setZoneLocation(player, args[1]);
                break;
            case "list":
                listZones(player);
                break;
            case "reload":
                reloadZones(player);
                break;
            case "help":
                showZoneHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다. /zone help를 사용하세요.");
                break;
        }

        return true;
    }

    /**
     * 점령지 위치 설정
     */
    private void setZoneLocation(Player player, String zoneName) {
        Location playerLoc = player.getLocation();
        
        // 점령지 타입 확인
        CaptureZone.ZoneType zoneType = getZoneType(zoneName);
        if (zoneType == null) {
            player.sendMessage(ChatColor.RED + "알 수 없는 점령지 이름입니다: " + zoneName);
            player.sendMessage(ChatColor.YELLOW + "사용 가능한 점령지: center, water, fire, wind, ice");
            return;
        }

        // 설정 파일에 저장
        String path = "zones." + zoneName;
        zonesConfig.set(path + ".world", playerLoc.getWorld().getName());
        zonesConfig.set(path + ".x", playerLoc.getBlockX());
        zonesConfig.set(path + ".y", playerLoc.getBlockY());
        zonesConfig.set(path + ".z", playerLoc.getBlockZ());
        zonesConfig.set(path + ".radius", 2.5);
        zonesConfig.set(path + ".type", zoneType.name());

        try {
            zonesConfig.save(zonesFile);
            
            // 점령지 생성
            CaptureZone zone = new CaptureZone(zoneName, zoneType, playerLoc, 2.5);
            captureManager.createCaptureZone(zoneName, zoneType, playerLoc, 2.5);
            
            // 신호기 자동 생성
            beaconManager.createBeaconStructure(zone, player);
            
            player.sendMessage(ChatColor.GREEN + zoneName + " 점령지와 신호기를 설정했습니다!");
            player.sendMessage(ChatColor.YELLOW + "위치: " + playerLoc.getBlockX() + ", " + playerLoc.getBlockY() + ", " + playerLoc.getBlockZ());
            player.sendMessage(ChatColor.YELLOW + "신호기가 자동으로 생성되었습니다.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "설정 파일 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 점령지 목록 표시
     */
    private void listZones(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 점령지 목록 ===");
        
        for (String zoneName : zonesConfig.getConfigurationSection("zones").getKeys(false)) {
            String path = "zones." + zoneName;
            String world = zonesConfig.getString(path + ".world", "world");
            int x = zonesConfig.getInt(path + ".x", 0);
            int y = zonesConfig.getInt(path + ".y", 64);
            int z = zonesConfig.getInt(path + ".z", 0);
            double radius = zonesConfig.getDouble(path + ".radius", 2.5);
            String type = zonesConfig.getString(path + ".type", "CENTER");
            
            player.sendMessage(ChatColor.WHITE + "• " + zoneName + " (" + type + ")");
            player.sendMessage(ChatColor.GRAY + "  위치: " + world + " " + x + ", " + y + ", " + z);
            player.sendMessage(ChatColor.GRAY + "  크기: " + radius + " 블록 반지름");
        }
    }

    /**
     * 점령지 설정 다시 로드
     */
    private void reloadZones(Player player) {
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
        player.sendMessage(ChatColor.GREEN + "점령지 설정을 다시 로드했습니다!");
        player.sendMessage(ChatColor.YELLOW + "서버 재시작 후 적용됩니다.");
    }

    /**
     * 도움말 표시
     */
    private void showZoneHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 점령지 및 신호기 설정 명령어 ===");
        player.sendMessage(ChatColor.WHITE + "/zone set <점령지이름> - 현재 위치에 점령지와 신호기 설정");
        player.sendMessage(ChatColor.WHITE + "/zone list - 점령지 목록 보기");
        player.sendMessage(ChatColor.WHITE + "/zone reload - 설정 다시 로드");
        player.sendMessage(ChatColor.WHITE + "/zone help - 도움말 보기");
        player.sendMessage(ChatColor.YELLOW + "점령지 이름: center, water, fire, wind, ice");
        player.sendMessage(ChatColor.GRAY + "신호기는 점령지 설정 시 자동으로 생성됩니다.");
    }

    /**
     * 점령지 이름으로 타입 반환
     */
    private CaptureZone.ZoneType getZoneType(String zoneName) {
        switch (zoneName.toLowerCase()) {
            case "center": return CaptureZone.ZoneType.CENTER;
            case "water": return CaptureZone.ZoneType.WATER;
            case "fire": return CaptureZone.ZoneType.FIRE;
            case "wind": return CaptureZone.ZoneType.WIND;
            case "ice": return CaptureZone.ZoneType.ICE;
            default: return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            if ("set".startsWith(input)) completions.add("set");
            if ("list".startsWith(input)) completions.add("list");
            if ("reload".startsWith(input)) completions.add("reload");
            if ("help".startsWith(input)) completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            String input = args[1].toLowerCase();
            if ("center".startsWith(input)) completions.add("center");
            if ("water".startsWith(input)) completions.add("water");
            if ("fire".startsWith(input)) completions.add("fire");
            if ("wind".startsWith(input)) completions.add("wind");
            if ("ice".startsWith(input)) completions.add("ice");
        }

        return completions;
    }
}
