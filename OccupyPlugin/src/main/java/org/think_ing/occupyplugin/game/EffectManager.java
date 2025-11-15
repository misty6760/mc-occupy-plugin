package org.think_ing.occupyplugin.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

import java.util.List;

/**
 * 효과 관리자
 * 플레이어에게 버프/디버프를 적용합니다
 */
public class EffectManager {
    
    private final CaptureSystem captureSystem;
    
    public EffectManager(CaptureSystem captureSystem) {
        this.captureSystem = captureSystem;
    }
    
    /**
     * 모든 플레이어에게 효과 적용
     * @param occupationPoints 점령지 리스트
     */
    public void updatePlayerEffects(List<OccupationPoint> occupationPoints) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team playerTeam = captureSystem.getPlayerTeam(player);
            if (playerTeam == null) {
                continue;
            }
            
            applyEffectsForPlayer(player, playerTeam, occupationPoints);
        }
    }
    
    /**
     * 개별 플레이어에게 효과 적용
     */
    private void applyEffectsForPlayer(Player player, Team playerTeam, List<OccupationPoint> occupationPoints) {
        for (OccupationPoint point : occupationPoints) {
            // 얼음 점령지 특수 로직
            if (point.getName().equals("얼음")) {
                applyIcePointEffects(player, playerTeam, point);
                continue;
            }
            
            // 일반 점령지 로직
            if (point.getOwner() == null) {
                continue;
            }
            
            if (point.getOwner().equals(playerTeam)) {
                // 자기 팀이 점령한 곳 - 버프 적용
                applyBuffs(player, point);
            } else if (captureSystem.isPlayerInPoint(player, point)) {
                // 적 팀이 점령한 곳에 있음 - 디버프 적용
                applyDebuffs(player, point);
            }
        }
    }
    
    /**
     * 얼음 점령지 특수 효과 적용
     * - 점령 전: 지역 내 모든 팀에게 구속 버프
     * - 점령 후: 적팀에게만 구속 버프 (아군은 면역)
     */
    private void applyIcePointEffects(Player player, Team playerTeam, OccupationPoint icePoint) {
        boolean isInZone = captureSystem.isPlayerInPoint(player, icePoint);
        
        if (!isInZone) {
            return;
        }
        
        if (icePoint.getOwner() == null) {
            // 점령 전: 지역 내 모든 팀에게 디버프
            applyDebuffs(player, icePoint);
        } else if (!icePoint.getOwner().equals(playerTeam)) {
            // 점령 후: 적팀에게만 디버프 (아군은 면역)
            applyDebuffs(player, icePoint);
        }
        // else: 아군은 면역 (아무것도 안 함)
    }
    
    /**
     * 버프 적용
     */
    private void applyBuffs(Player player, OccupationPoint point) {
        for (PotionEffect effect : point.getEffects()) {
            player.addPotionEffect(effect);
        }
    }
    
    /**
     * 디버프 적용
     */
    private void applyDebuffs(Player player, OccupationPoint point) {
        for (PotionEffect debuff : point.getDebuffs()) {
            player.addPotionEffect(debuff);
        }
    }
}
