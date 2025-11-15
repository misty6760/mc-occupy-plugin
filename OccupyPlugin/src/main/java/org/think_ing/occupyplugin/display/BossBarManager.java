package org.think_ing.occupyplugin.display;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.think_ing.occupyplugin.OccupyPlugin;
import org.think_ing.occupyplugin.game.OccupationPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {

    private final OccupyPlugin plugin;
    private BossBar centerBossBar;
    private final Map<UUID, BossBar> captureBossBars = new HashMap<>();

    public BossBarManager(OccupyPlugin plugin) {
        this.plugin = plugin;
    }

    public void createCenterBossBar() {
        centerBossBar = Bukkit.createBossBar("[ 중앙 점령지 현황 ]", BarColor.BLUE, BarStyle.SOLID);
        centerBossBar.setVisible(true);
    }

    public void updateCenterBossBar(OccupationPoint centerPoint) {
        if (centerBossBar != null) {
            if (centerPoint.getOwner() != null) {
                @SuppressWarnings("deprecation")
                String ownerDisplayName = centerPoint.getOwner().getDisplayName();
                centerBossBar.setTitle("[ " + centerPoint.getName() + " ] - 점령팀: " + ownerDisplayName + " 팀");
                @SuppressWarnings("deprecation")
                ChatColor ownerColor = centerPoint.getOwner().getColor();
                centerBossBar.setColor(getBarColor(ownerColor));
                centerBossBar.setProgress(1.0);
            } else {
                if (centerPoint.getCaptureProgress() > 0 && !centerPoint.getPlayersInZone().isEmpty()) {
                    centerPoint.getPlayersInZone().stream()
                            .findFirst()
                            .ifPresent(player -> {
                                Team team = plugin.getGameManager().getPlayerTeam(player);
                                if (team != null) {
                                    @SuppressWarnings("deprecation")
                                    String teamDisplayName = team.getDisplayName();
                                    centerBossBar.setTitle("[ " + centerPoint.getName() + " ] - " + teamDisplayName + " 팀이 점령 중...");
                                    @SuppressWarnings("deprecation")
                                    ChatColor teamColor = team.getColor();
                                    centerBossBar.setColor(getBarColor(teamColor));
                                    double timeToCapture = centerPoint.getCaptureTime();
                                    double progress = centerPoint.getCaptureProgress() / timeToCapture;
                                    centerBossBar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
                                }
                            });
                } else {
                    centerBossBar.setTitle("[ " + centerPoint.getName() + " ] - 중립 상태");
                    centerBossBar.setColor(BarColor.WHITE);
                    centerBossBar.setProgress(1.0);
                }
            }
            // Add all online players to the boss bar
            for (Player player : Bukkit.getOnlinePlayers()) {
                centerBossBar.addPlayer(player);
            }
        }
    }

    public void hideCenterBossBar() {
        if (centerBossBar != null) {
            centerBossBar.removeAll();
            centerBossBar.setVisible(false);
            centerBossBar = null;
        }
    }

    public void showCaptureBossBar(Player player, OccupationPoint point) {
        BossBar bossBar = captureBossBars.get(player.getUniqueId());
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
            captureBossBars.put(player.getUniqueId(), bossBar);
        }

        bossBar.setTitle(point.getTextColor() + point.getName() + " 점령 중...");
        bossBar.setColor(point.getBarColor());

        double timeToCapture = (point.getOwner() == null) ? point.getCaptureTime() : point.getRecaptureTime();
        if (timeToCapture <= 0) {
            bossBar.setProgress(1.0);
        } else {
            double progress = point.getCaptureProgress() / timeToCapture;
            bossBar.setProgress(Math.min(1.0, Math.max(0.0, progress)));
        }

        bossBar.addPlayer(player);
        bossBar.setVisible(true);
    }

    public void hideCaptureBossBar(Player player) {
        BossBar bar = captureBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.setVisible(false);
            bar.removeAll();
        }
    }

    private BarColor getBarColor(ChatColor chatColor) {
        switch (chatColor) {
            case RED:
            case DARK_RED:
                return BarColor.RED;
            case BLUE:
            case DARK_BLUE:
            case AQUA:
                return BarColor.BLUE;
            case GREEN:
            case DARK_GREEN:
                return BarColor.GREEN;
            case YELLOW:
            case GOLD:
                return BarColor.YELLOW;
            case LIGHT_PURPLE:
            case DARK_PURPLE:
                return BarColor.PURPLE;
            case WHITE:
            default:
                return BarColor.WHITE;
        }
    }
}
