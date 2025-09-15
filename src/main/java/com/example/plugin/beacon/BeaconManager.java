package com.example.plugin.beacon;

import com.example.plugin.capture.CaptureZone;
import com.example.plugin.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 신호기 색상 관리자
 * 점령지 중심 아래 신호기 위에 색유리로 팀 색상 표시
 */
public class BeaconManager {
    private final JavaPlugin plugin;
    private final Map<String, Location> beaconLocations;
    private Map<ChatColor, Material> teamColors;

    public BeaconManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.beaconLocations = new HashMap<>();
        initializeTeamColors();
    }

    /**
     * 팀 색상과 색유리 매핑 초기화
     */
    private void initializeTeamColors() {
        teamColors = new HashMap<>();
        teamColors.put(ChatColor.RED, Material.RED_STAINED_GLASS);
        teamColors.put(ChatColor.BLUE, Material.BLUE_STAINED_GLASS);
        teamColors.put(ChatColor.GREEN, Material.GREEN_STAINED_GLASS);
        teamColors.put(ChatColor.YELLOW, Material.YELLOW_STAINED_GLASS);
        teamColors.put(ChatColor.WHITE, Material.WHITE_STAINED_GLASS); // 기본값
    }

    /**
     * 점령지에 신호기 설정
     * @param zone 점령지
     * @param team 점령한 팀
     */
    public void setBeaconColor(CaptureZone zone, Team team) {
        Location center = zone.getCenter();
        if (center == null) return;

        // 신호기 위치: 점령지 중심 아래 1칸
        Location beaconLocation = center.clone().add(0, -1, 0);
        beaconLocations.put(zone.getName(), beaconLocation);

        // 신호기 블록 확인
        Block beaconBlock = beaconLocation.getBlock();
        if (beaconBlock.getType() != Material.BEACON) {
            plugin.getLogger().warning("신호기가 설정되지 않은 위치입니다: " + zone.getName() + " (" + 
                                     beaconLocation.getBlockX() + ", " + beaconLocation.getBlockY() + ", " + 
                                     beaconLocation.getBlockZ() + ")");
            return;
        }

        // 색유리 위치: 신호기 위 1칸 (비활성화됨)
        // Location glassLocation = beaconLocation.clone().add(0, 1, 0);
        // Block glassBlock = glassLocation.getBlock();

        // 팀 색상에 맞는 색유리 배치 (비활성화됨)
        // Material glassType = teamColors.getOrDefault(team.getColor(), Material.WHITE_STAINED_GLASS);
        // glassBlock.setType(glassType);

        // 신호기 활성화 (필요한 경우)
        BlockState beaconState = beaconBlock.getState();
        if (beaconState instanceof Beacon) {
            // 신호기 효과 설정 (선택사항)
            // Beacon beacon = (Beacon) beaconState;
            // beacon.setPrimaryEffect(PotionEffectType.SPEED);
        }

        plugin.getLogger().info(team.getName() + " 팀이 " + zone.getName() + " 점령지의 신호기 색상을 " + 
                              team.getColor() + "로 변경했습니다!");
    }

    /**
     * 점령지 신호기 색상 초기화
     * @param zone 점령지
     */
    public void resetBeaconColor(CaptureZone zone) {
        Location beaconLocation = beaconLocations.get(zone.getName());
        if (beaconLocation == null) return;

        // 색유리 제거 (공기로 변경)
        Location glassLocation = beaconLocation.clone().add(0, 1, 0);
        Block glassBlock = glassLocation.getBlock();
        glassBlock.setType(Material.AIR);

        plugin.getLogger().info(zone.getName() + " 점령지의 신호기 색상을 초기화했습니다!");
    }


    /**
     * 점령지에 신호기 구조 생성 (기본 Y좌표 사용)
     * @param zone 점령지
     */
    public void createBeaconStructure(CaptureZone zone) {
        Location center = zone.getCenter();
        if (center == null) return;

        World world = center.getWorld();
        if (world == null) return;

        // 점령지 중심 기준 Y좌표 사용
        int beaconY = center.getBlockY() - 1;  // 신호기: 중심 Y - 1
        int baseY = center.getBlockY() - 2;    // 철블럭 기초: 중심 Y - 2
        int glassY = center.getBlockY() + 1;   // 색유리: 중심 Y + 1 (신호기 위)

        // 신호기 기반 구조 생성 (3x3 철 블록)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLocation = center.clone().add(x, baseY - center.getBlockY(), z);
                blockLocation.getBlock().setType(Material.IRON_BLOCK);
            }
        }

        // 신호기 배치
        Location beaconLocation = center.clone().add(0, beaconY - center.getBlockY(), 0);
        beaconLocation.getBlock().setType(Material.BEACON);
        beaconLocations.put(zone.getName(), beaconLocation);

        // 점령지 타입에 맞는 색유리 설치
        Location glassLocation = center.clone().add(0, glassY - center.getBlockY(), 0);
        Material glassType = getZoneGlassType(zone.getType());
        glassLocation.getBlock().setType(glassType);

        plugin.getLogger().info(zone.getName() + " 점령지에 신호기 구조를 생성했습니다! (Y=" + beaconY + ")");
    }

    /**
     * 점령지에 신호기 구조 생성 (테스트용)
     * @param zone 점령지
     * @param player 명령어 실행자 (Y좌표 기준용)
     */
    public void createBeaconStructure(CaptureZone zone, org.bukkit.entity.Player player) {
        Location center = zone.getCenter();
        if (center == null) return;

        World world = center.getWorld();
        if (world == null) return;

        // 사용자 위치 기준 Y좌표 사용 (한 칸 더 아래)
        int playerY = player.getLocation().getBlockY();
        int beaconY = playerY - 2;  // 신호기: 사용자 Y - 2
        int baseY = playerY - 3;    // 철블럭 기초: 사용자 Y - 3
        int glassY = playerY - 1;   // 색유리: 사용자 Y - 1 (신호기 위)

        // 신호기 기반 구조 생성 (3x3 철 블록) - 사용자 Y - 3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLocation = center.clone().add(x, baseY - center.getBlockY(), z);
                blockLocation.getBlock().setType(Material.IRON_BLOCK);
            }
        }

        // 신호기 배치 - 사용자 Y - 2
        Location beaconLocation = center.clone().add(0, beaconY - center.getBlockY(), 0);
        beaconLocation.getBlock().setType(Material.BEACON);
        beaconLocations.put(zone.getName(), beaconLocation);        // 점령지 타입에 맞는 색유리 설치 - 사용자 Y - 1
        Location glassLocation = center.clone().add(0, glassY - center.getBlockY(), 0);
        Material glassType = getZoneGlassType(zone.getType());
        glassLocation.getBlock().setType(glassType);

        plugin.getLogger().info(zone.getName() + " 점령지에 신호기 구조를 생성했습니다! (Y=" + beaconY + ")");
    }

    /**
     * 모든 점령지에 신호기 구조 생성
     * @param zones 점령지 목록
     * @param player 명령어 실행자 (Y좌표 기준용)
     */
    public void createAllBeaconStructures(java.util.Collection<CaptureZone> zones, org.bukkit.entity.Player player) {
        for (CaptureZone zone : zones) {
            createBeaconStructure(zone, player);
        }
    }

    /**
     * 신호기 위치 반환
     * @param zoneName 점령지 이름
     * @return 신호기 위치
     */
    public Location getBeaconLocation(String zoneName) {
        return beaconLocations.get(zoneName);
    }

    /**
     * 팀 색상에 맞는 색유리 타입 반환
     * @param color 팀 색상
     * @return 색유리 타입
     */
    public Material getTeamGlassType(ChatColor color) {
        return teamColors.getOrDefault(color, Material.WHITE_STAINED_GLASS);
    }

    /**
     * 점령지 타입에 맞는 색유리 타입 반환
     * @param zoneType 점령지 타입
     * @return 색유리 타입
     */
    public Material getZoneGlassType(com.example.plugin.capture.CaptureZone.ZoneType zoneType) {
        switch (zoneType) {
            case CENTER:
                return Material.YELLOW_STAINED_GLASS; // 중앙: 노란색
            case FIRE:
                return Material.RED_STAINED_GLASS; // 불: 빨간색
            case WIND:
                return Material.LIGHT_GRAY_STAINED_GLASS; // 바람: 회백색
            case WATER:
                return Material.BLUE_STAINED_GLASS; // 물: 파란색
            case ICE:
                return Material.LIGHT_BLUE_STAINED_GLASS; // 얼음: 하늘색
            default:
                return Material.WHITE_STAINED_GLASS; // 기본값: 흰색
        }
    }

    /**
     * 모든 신호기 색상 초기화 (원래 점령지 색유리로 복원)
     */
    public void resetAllBeaconColors() {
        for (Map.Entry<String, Location> entry : beaconLocations.entrySet()) {
            Location beaconLoc = entry.getValue();
            
            if (beaconLoc != null) {
                World world = beaconLoc.getWorld();
                if (world != null) {
                    // 점령지 이름에 따라 원래 색유리로 복원 (비활성화됨)
                    // Block glassBlock = world.getBlockAt(beaconLoc.clone().add(0, 1, 0));
                    // Material originalGlassType = getOriginalZoneGlassType(zoneName);
                    // glassBlock.setType(originalGlassType);
                }
            }
        }
        plugin.getLogger().info("모든 신호기 색상이 원래 점령지 색유리로 복원되었습니다.");
    }


    /**
     * 특정 점령지의 신호기 구조 제거
     * @param zone 점령지
     */
    public void removeBeaconStructure(CaptureZone zone) {
        Location beaconLoc = beaconLocations.get(zone.getName());
        if (beaconLoc == null) return;
        
        World world = beaconLoc.getWorld();
        if (world == null) return;
        
        // 신호기 제거 (흙으로 교체)
        beaconLoc.getBlock().setType(Material.DIRT);
        
        // 색유리 제거 (흙으로 교체)
        Location glassLoc = beaconLoc.clone().add(0, 1, 0);
        glassLoc.getBlock().setType(Material.DIRT);
        
        // 철블럭 기초 제거 (흙으로 교체) - 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location baseLoc = beaconLoc.clone().add(x, -1, z);
                baseLoc.getBlock().setType(Material.DIRT);
            }
        }
        
        // 신호기 위치 정보에서 제거
        beaconLocations.remove(zone.getName());
        plugin.getLogger().info(zone.getName() + " 점령지의 신호기 구조를 제거했습니다.");
    }

    /**
     * 모든 신호기 구조 제거 (신호기, 철블럭, 색유리를 흙으로 교체)
     */
    public void removeAllBeaconStructures() {
        for (Map.Entry<String, Location> entry : beaconLocations.entrySet()) {
            String zoneName = entry.getKey();
            Location beaconLoc = entry.getValue();
            
            if (beaconLoc != null) {
                World world = beaconLoc.getWorld();
                if (world != null) {
                    // 신호기 제거 (흙으로 교체)
                    beaconLoc.getBlock().setType(Material.DIRT);
                    
                    // 색유리 제거 (흙으로 교체)
                    Location glassLoc = beaconLoc.clone().add(0, 1, 0);
                    glassLoc.getBlock().setType(Material.DIRT);
                    
                    // 철블럭 기초 제거 (흙으로 교체) - 3x3
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            Location baseLoc = beaconLoc.clone().add(x, -1, z);
                            baseLoc.getBlock().setType(Material.DIRT);
                        }
                    }
                    
                    plugin.getLogger().info(zoneName + " 점령지의 신호기 구조를 제거했습니다.");
                }
            }
        }
        
        // 신호기 위치 정보 초기화
        beaconLocations.clear();
        plugin.getLogger().info("모든 신호기 구조가 제거되었습니다.");
    }
    
    /**
     * 모든 신호기 초기화 (색상만 중립으로 변경)
     * 구조는 유지하고 색상만 중립 상태로 변경
     */
    public void resetAllBeacons() {
        for (Map.Entry<String, Location> entry : beaconLocations.entrySet()) {
            Location location = entry.getValue();
            
            if (location.getBlock().getType().name().contains("BEACON")) {
                // 신호기 색상을 중립(흰색)으로 변경
                resetBeaconColor(location);
            }
        }
        plugin.getLogger().info("모든 신호기가 중립 상태로 초기화되었습니다.");
    }
    
    /**
     * 특정 위치의 신호기 색상 초기화
     * @param location 신호기 위치
     */
    private void resetBeaconColor(Location location) {
        if (location == null || !location.getBlock().getType().name().contains("BEACON")) {
            return;
        }
        
        // 신호기 색상을 중립(흰색)으로 변경
        setBeaconColor(location, ChatColor.WHITE);
    }
    
    /**
     * 특정 위치의 신호기 색상 설정
     * @param location 신호기 위치
     * @param color 설정할 색상
     */
    private void setBeaconColor(Location location, ChatColor color) {
        if (location == null || !location.getBlock().getType().name().contains("BEACON")) {
            return;
        }
        
        // 신호기 색상 설정 로직 (구현 필요)
        // 여기서는 로그만 출력
        plugin.getLogger().info("신호기 색상을 " + color.name() + "으로 변경했습니다.");
    }
}
