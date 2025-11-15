package org.think_ing.occupyplugin.game;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * 비콘 관리자
 * 비콘 색상 변경 로직을 처리합니다
 */
public class BeaconManager {
    
    /**
     * 팀 색상에 맞는 색유리 Material 반환
     * @param color 팀 색상
     * @return 해당하는 색유리 Material
     */
    public Material getStainedGlass(ChatColor color) {
        switch (color) {
            case BLACK:
                return Material.BLACK_STAINED_GLASS;
            case DARK_BLUE:
                return Material.BLUE_STAINED_GLASS;
            case DARK_GREEN:
                return Material.GREEN_STAINED_GLASS;
            case DARK_AQUA:
                return Material.CYAN_STAINED_GLASS;
            case DARK_RED:
                return Material.RED_STAINED_GLASS;
            case DARK_PURPLE:
                return Material.PURPLE_STAINED_GLASS;
            case GOLD:
                return Material.ORANGE_STAINED_GLASS;
            case GRAY:
                return Material.LIGHT_GRAY_STAINED_GLASS;
            case DARK_GRAY:
                return Material.GRAY_STAINED_GLASS;
            case BLUE:
                return Material.LIGHT_BLUE_STAINED_GLASS;
            case GREEN:
                return Material.LIME_STAINED_GLASS;
            case AQUA:
                return Material.LIGHT_BLUE_STAINED_GLASS;
            case RED:
                return Material.RED_STAINED_GLASS;
            case LIGHT_PURPLE:
                return Material.MAGENTA_STAINED_GLASS;
            case YELLOW:
                return Material.YELLOW_STAINED_GLASS;
            case WHITE:
                return Material.WHITE_STAINED_GLASS;
            default:
                return Material.GLASS;
        }
    }
}

