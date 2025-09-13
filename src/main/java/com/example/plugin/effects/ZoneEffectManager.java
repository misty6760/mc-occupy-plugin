package com.example.plugin.effects;

import com.example.plugin.capture.CaptureZone;
import com.example.plugin.team.Team;
import com.example.plugin.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 점령지별 효과 관리자 클래스
 * 각 점령지의 특수 효과와 환경을 관리
 */
public class ZoneEffectManager {
    private final JavaPlugin plugin;
    private final Map<String, BukkitRunnable> effectTasks;
    private final Map<String, List<Location>> zoneBlocks; // 점령지별 블록 위치들

    public ZoneEffectManager(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.effectTasks = new HashMap<>();
        this.zoneBlocks = new HashMap<>();
    }

    /**
     * 점령지 효과 적용
     * @param zone 점령지
     * @param team 점령한 팀
     */
    public void applyZoneEffects(CaptureZone zone, Team team) {
        stopZoneEffects(zone.getName());
        
        switch (zone.getType()) {
            case FIRE:
                applyFireZoneEffects(zone, team);
                break;
            case WATER:
                applyWaterZoneEffects(zone, team);
                break;
            case ICE:
                applyIceZoneEffects(zone, team);
                break;
            case WIND:
                applyWindZoneEffects(zone, team);
                break;
            case CENTER:
                applyCenterZoneEffects(zone, team);
                break;
        }
    }

    /**
     * 불 점령지 효과 적용
     * @param zone 점령지
     * @param team 점령한 팀
     */
    private void applyFireZoneEffects(CaptureZone zone, Team team) {
        // 용암 지대 생성 제거 - 효과만 적용
        
        // 팀원들에게 화염 저항 효과 부여
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (zone.isPlayerInZone(player) && team.hasMember(player)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0));
                        
                        // 파티클 효과
                        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L); // 1초마다 실행
        effectTasks.put(zone.getName(), task);
    }

    /**
     * 물 점령지 효과 적용
     * @param zone 점령지
     * @param team 점령한 팀
     */
    private void applyWaterZoneEffects(CaptureZone zone, Team team) {
        // 물 지대 생성 제거 - 효과만 적용
        
        // 팀원들에게 수중 호흡과 돌고래의 가호 효과 부여
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (zone.isPlayerInZone(player) && team.hasMember(player)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 100, 0));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100, 0));
                        
                        // 물 파티클 효과
                        player.getWorld().spawnParticle(Particle.WATER_SPLASH, player.getLocation(), 15, 1.0, 1.0, 1.0, 0.1);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        effectTasks.put(zone.getName(), task);
    }

    /**
     * 얼음 점령지 효과 적용
     * @param zone 점령지
     * @param team 점령한 팀
     */
    private void applyIceZoneEffects(CaptureZone zone, Team team) {
        // 얼음 지대 생성 제거 - 효과만 적용
        
        // 적에게 구속 효과, 아군은 면역
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (zone.isPlayerInZone(player)) {
                        if (team.hasMember(player)) {
                            // 아군은 면역
                            player.removePotionEffect(PotionEffectType.SLOW);
                        } else {
                            // 적에게 구속 효과
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                        }
                        
                        // 얼음 파티클 효과
                        player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(), 20, 1.0, 1.0, 1.0, 0.1);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        effectTasks.put(zone.getName(), task);
    }

    /**
     * 얼음 지역 기본 디버프 적용 (미점령 또는 상대 팀 점령 시)
     * @param zone 점령지
     * @param currentTeam 현재 점령한 팀 (null이면 미점령)
     */
    public void applyIceZoneDefaultDebuff(CaptureZone zone, Team currentTeam) {
        stopZoneEffects(zone.getName());
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (zone.isPlayerInZone(player)) {
                        if (currentTeam != null && currentTeam.hasMember(player)) {
                            // 아군은 면역
                            player.removePotionEffect(PotionEffectType.SLOW);
                        } else {
                            // 미점령 또는 상대 팀 점령 시 구속 1 디버프
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
                        }
                        
                        // 얼음 파티클 효과
                        player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(), 20, 1.0, 1.0, 1.0, 0.1);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        effectTasks.put(zone.getName(), task);
    }

    /**
     * 바람 점령지 효과 적용
     * @param zone 점령지
     * @param team 점령한 팀
     */
    private void applyWindZoneEffects(CaptureZone zone, Team team) {
        // 점프 맵 구조 생성 제거 - 효과만 적용
        
        // 팀원들에게 신속 효과 부여
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (zone.isPlayerInZone(player) && team.hasMember(player)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
                        
                        // 바람 파티클 효과
                        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        effectTasks.put(zone.getName(), task);
    }

    /**
     * 중앙 점령지 효과 적용
     * @param zone 점령지
     * @param team 점령한 팀
     */
    private void applyCenterZoneEffects(CaptureZone zone, Team team) {
        // 전장 환경 생성
        createBattleZone(zone);
        
        // 특별한 효과는 없지만 전투에 유리한 환경 제공
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (zone.isPlayerInZone(player)) {
                        // 전투 파티클 효과
                        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        effectTasks.put(zone.getName(), task);
    }





    /**
     * 전장 환경 생성
     * @param zone 점령지
     */
    private void createBattleZone(CaptureZone zone) {
        Location center = zone.getCenter();
        int radius = (int) zone.getRadius();
        List<Location> blocks = new ArrayList<>();
        
        // 전장 환경 (장애물, 엄폐물 등)
        for (int x = -radius; x <= radius; x += 5) {
            for (int z = -radius; z <= radius; z += 5) {
                if (x * x + z * z <= radius * radius) {
                    Location loc = center.clone().add(x, 0, z);
                    if (loc.getBlock().getType() == Material.AIR) {
                        loc.getBlock().setType(Material.COBBLESTONE);
                        blocks.add(loc);
                    }
                }
            }
        }
        
        zoneBlocks.put(zone.getName(), blocks);
    }

    /**
     * 점령지 효과 중단
     * @param zoneName 점령지 이름
     */
    public void stopZoneEffects(String zoneName) {
        BukkitRunnable task = effectTasks.remove(zoneName);
        if (task != null) {
            task.cancel();
        }
        
        // 블록 복원
        restoreZoneBlocks(zoneName);
    }

    /**
     * 모든 점령지 효과 중단
     */
    public void stopAllZoneEffects() {
        for (String zoneName : new HashSet<>(effectTasks.keySet())) {
            stopZoneEffects(zoneName);
        }
    }

    /**
     * 점령지 블록 복원
     * @param zoneName 점령지 이름
     */
    private void restoreZoneBlocks(String zoneName) {
        List<Location> blocks = zoneBlocks.remove(zoneName);
        if (blocks != null) {
            for (Location loc : blocks) {
                loc.getBlock().setType(Material.AIR);
            }
        }
    }

    /**
     * 모든 효과 중단
     */
    public void stopAllEffects() {
        for (BukkitRunnable task : effectTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        effectTasks.clear();
        
        // 모든 블록 복원
        for (String zoneName : zoneBlocks.keySet()) {
            restoreZoneBlocks(zoneName);
        }
        zoneBlocks.clear();
    }
}
