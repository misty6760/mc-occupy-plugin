package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.OccupyPlugin;
import org.think_ing.occupyplugin.commands.SubCommand;
import org.think_ing.occupyplugin.game.GameManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 점령지 위치 설정 명령어
 */
public class SetPointCommand implements SubCommand {

    private final OccupyPlugin plugin;
    private final GameManager gameManager;

    public SetPointCommand(OccupyPlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (gameManager.isGameRunning()) {
            sender.sendMessage(ChatColor.RED + "게임이 진행 중일 때는 점령지 위치를 설정할 수 없습니다.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + getUsage());
            return true;
        }

        Player player = (Player) sender;
        String pointName = args[1].toLowerCase();

        ConfigurationSection pointsSection = plugin.getConfig()
                .getConfigurationSection("occupation_points");
        if (pointsSection == null) {
            sender.sendMessage(ChatColor.RED + "설정에서 점령 지점을 찾을 수 없습니다.");
            return true;
        }

        Set<String> validPoints = pointsSection.getKeys(false);

        if (!validPoints.contains(pointName)) {
            sender.sendMessage(ChatColor.RED + "잘못된 지점 이름입니다. 다음 중 하나를 사용하세요: "
                    + String.join(", ", validPoints));
            return true;
        }

        Location newLocation = player.getLocation();

        // Check for location conflicts with other points
        if (hasLocationConflict(sender, pointsSection, validPoints, pointName, newLocation)) {
            return true;
        }

        // Save location
        String newLocationString = newLocation.getWorld().getName() + ","
                + newLocation.getX() + "," + newLocation.getY() + "," + newLocation.getZ();
        plugin.getConfig().set("occupation_points." + pointName + ".location", newLocationString);
        plugin.saveConfig();
        gameManager.loadConfig();

        // 신호기 설치
        String textColor = pointsSection.getString(pointName + ".text_color", "WHITE");
        buildBeaconStructure(newLocation, textColor);

        String pointDisplayName = pointsSection.getString(pointName + ".name", pointName);
        sender.sendMessage(ChatColor.GREEN + "점령 지점 '" + pointDisplayName + "'의 위치를 현재 위치로 설정했습니다.");
        sender.sendMessage(ChatColor.GREEN + "신호기 구조물을 설치했습니다.");

        return true;
    }

    /**
     * 신호기 구조물을 설치합니다
     * 구조 (지표면에서 지하로): 색상 유리(지표면) -> 신호기 -> 철블록 9개(3x3)
     */
    private void buildBeaconStructure(Location centerLocation, String colorName) {
        Location baseLocation = centerLocation.clone();
        int baseX = baseLocation.getBlockX();
        int baseY = baseLocation.getBlockY();
        int baseZ = baseLocation.getBlockZ();

        // 1. 색상 유리 설치 (Y, 지표면 중앙)
        Block glassBlock = baseLocation.getWorld().getBlockAt(baseX, baseY - 1, baseZ);
        Material glassMaterial = getStainedGlassByColor(colorName);
        glassBlock.setType(glassMaterial);

        // 2. 신호기 설치 (Y-1, 중앙)
        Block beaconBlock = baseLocation.getWorld().getBlockAt(baseX, baseY - 2, baseZ);
        beaconBlock.setType(Material.BEACON);

        // 3. 철블록 9개 설치 (Y-2, 3x3)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block block = baseLocation.getWorld().getBlockAt(baseX + x, baseY - 3, baseZ + z);
                block.setType(Material.IRON_BLOCK);
            }
        }
    }

    /**
     * 색상 이름에 따른 색유리 Material 반환
     */
    private Material getStainedGlassByColor(String colorName) {
        switch (colorName.toUpperCase()) {
            case "RED":
                return Material.RED_STAINED_GLASS;
            case "BLUE":
                return Material.BLUE_STAINED_GLASS;
            case "AQUA":
            case "CYAN":
                return Material.CYAN_STAINED_GLASS;
            case "YELLOW":
                return Material.YELLOW_STAINED_GLASS;
            case "WHITE":
            default:
                return Material.WHITE_STAINED_GLASS;
        }
    }

    private boolean hasLocationConflict(CommandSender sender, ConfigurationSection pointsSection,
            Set<String> validPoints, String pointName, Location newLocation) {
        for (String otherPointName : validPoints) {
            if (otherPointName.equalsIgnoreCase(pointName)) {
                continue;
            }

            String locString = pointsSection.getString(otherPointName + ".location");
            if (locString != null) {
                String[] parts = locString.split(",");
                if (parts.length == 4) {
                    try {
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);

                        if (newLocation.getBlockX() == (int) x &&
                                newLocation.getBlockY() == (int) y &&
                                newLocation.getBlockZ() == (int) z) {

                            sender.sendMessage(ChatColor.RED + "다른 점령지('" + otherPointName
                                    + "')와 위치가 중복됩니다. 다른 위치를 지정해주세요.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid location strings
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "setpoint";
    }

    @Override
    public String getDescription() {
        return "점령지의 중심 위치를 설정합니다";
    }

    @Override
    public String getUsage() {
        return "/occupy setpoint <점령지이름>";
    }

    @Override
    public List<String> getTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            ConfigurationSection pointsSection = plugin.getConfig()
                    .getConfigurationSection("occupation_points");
            if (pointsSection != null) {
                return new ArrayList<>(pointsSection.getKeys(false));
            }
        }
        return Collections.emptyList();
    }
}
