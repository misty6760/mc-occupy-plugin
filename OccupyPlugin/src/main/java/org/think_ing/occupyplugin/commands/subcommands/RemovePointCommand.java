package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
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

/**
 * 점령지 위치 제거 명령어
 */
public class RemovePointCommand implements SubCommand {

    private final OccupyPlugin plugin;
    private final GameManager gameManager;

    public RemovePointCommand(OccupyPlugin plugin, GameManager gameManager) {
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
            sender.sendMessage(ChatColor.RED + "게임이 진행 중일 때는 점령지 위치를 삭제할 수 없습니다.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + getUsage());
            return true;
        }

        String pointName = args[1].toLowerCase();
        ConfigurationSection pointsSection = plugin.getConfig()
                .getConfigurationSection("occupation_points");

        if (pointsSection == null || !pointsSection.getKeys(false).contains(pointName)) {
            sender.sendMessage(ChatColor.RED + "잘못된 지점 이름입니다.");
            return true;
        }

        pointsSection.set(pointName + ".location", null);
        plugin.saveConfig();
        gameManager.loadConfig();

        sender.sendMessage(ChatColor.GREEN + "점령 지점 '" + pointName + "'의 위치 정보가 삭제되었습니다.");

        return true;
    }

    @Override
    public String getName() {
        return "removepoint";
    }

    @Override
    public String getDescription() {
        return "점령지의 위치 정보를 삭제합니다";
    }

    @Override
    public String getUsage() {
        return "/occupy removepoint <점령지이름>";
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
