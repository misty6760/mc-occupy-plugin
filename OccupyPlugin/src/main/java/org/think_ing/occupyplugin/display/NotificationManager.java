package org.think_ing.occupyplugin.display;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class NotificationManager {

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }
    
    @SuppressWarnings("deprecation")
    public void broadcastMessage(String message) {
        Bukkit.broadcastMessage(message);
    }
}
