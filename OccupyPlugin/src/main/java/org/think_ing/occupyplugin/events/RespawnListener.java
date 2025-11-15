package org.think_ing.occupyplugin.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.think_ing.occupyplugin.OccupyPlugin;

/**
 * 커스텀 리스폰 리스너
 * 플레이어가 리스폰할 때 Y100 좌표에서 느린 낙하 효과와 함께 스폰합니다
 */
public class RespawnListener implements Listener {
    
    private final OccupyPlugin plugin;
    private final boolean enabled;
    private final int yCoordinate;
    private final int slowFallingDuration;
    private final int slowFallingAmplifier;
    
    public RespawnListener(OccupyPlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("respawn.enabled", true);
        this.yCoordinate = plugin.getConfig().getInt("respawn.y_coordinate", 100);
        this.slowFallingDuration = plugin.getConfig().getInt("respawn.slow_falling_duration", 200);
        this.slowFallingAmplifier = plugin.getConfig().getInt("respawn.slow_falling_amplifier", 0);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!enabled) {
            return;
        }
        
        Player player = event.getPlayer();
        Location respawnLocation = event.getRespawnLocation();
        
        // Y좌표만 변경하여 하늘에서 스폰
        Location skyLocation = new Location(
                respawnLocation.getWorld(),
                respawnLocation.getX(),
                yCoordinate,
                respawnLocation.getZ(),
                respawnLocation.getYaw(),
                respawnLocation.getPitch()
        );
        
        event.setRespawnLocation(skyLocation);
        
        // 느린 낙하 효과 부여 (1틱 후에 적용)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                PotionEffect slowFalling = new PotionEffect(
                        PotionEffectType.SLOW_FALLING,
                        slowFallingDuration,
                        slowFallingAmplifier,
                        false,
                        false
                );
                player.addPotionEffect(slowFalling);
            }
        }, 1L);
    }
}

