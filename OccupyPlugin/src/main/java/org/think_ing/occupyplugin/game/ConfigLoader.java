package org.think_ing.occupyplugin.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.think_ing.occupyplugin.OccupyPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 설정 로더
 * config.yml에서 점령지 정보를 로드하고 파싱합니다
 */
public class ConfigLoader {
    
    private final OccupyPlugin plugin;
    
    public ConfigLoader(OccupyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 참여 팀 목록 로드
     * @return 참여 팀 이름 리스트
     */
    public List<String> loadParticipatingTeams() {
        return plugin.getConfig().getStringList("participating_teams");
    }
    
    /**
     * 점령지 목록 로드
     * @return 점령지 리스트
     */
    public List<OccupationPoint> loadOccupationPoints() {
        plugin.reloadConfig();
        List<OccupationPoint> occupationPoints = new ArrayList<>();
        
        ConfigurationSection pointsSection = plugin.getConfig().getConfigurationSection("occupation_points");
        if (pointsSection == null) {
            return occupationPoints;
        }
        
        for (String pointKey : pointsSection.getKeys(false)) {
            OccupationPoint point = loadOccupationPoint(pointsSection, pointKey);
            if (point != null) {
                occupationPoints.add(point);
            }
        }
        
        return occupationPoints;
    }
    
    /**
     * 개별 점령지 로드
     */
    private OccupationPoint loadOccupationPoint(ConfigurationSection pointsSection, String pointKey) {
        String name = pointsSection.getString(pointKey + ".name", "점령지");
        String locationString = pointsSection.getString(pointKey + ".location");
        double size = pointsSection.getDouble(pointKey + ".size", 15.0);
        int captureTime = pointsSection.getInt(pointKey + ".capture_time", 10);
        int recaptureTime = pointsSection.getInt(pointKey + ".recapture_time", 20);
        String beaconGlassLocationString = pointsSection.getString(pointKey + ".beacon_glass_location");
        ChatColor textColor = ChatColor.valueOf(
                pointsSection.getString(pointKey + ".text_color", "WHITE").toUpperCase());
        BarColor barColor = BarColor.valueOf(
                pointsSection.getString(pointKey + ".bar_color", "WHITE").toUpperCase());
        
        if (locationString == null) {
            plugin.getLogger().warning("점령 지점 '" + pointKey + "'의 위치가 설정되지 않았습니다. " +
                    "/occupy setpoint 명령어를 사용해 설정해주세요.");
            return null;
        }
        
        Location location = parseLocation(locationString);
        if (location == null) {
            return null;
        }
        
        Location beaconGlassLocation = (beaconGlassLocationString != null) 
                ? parseLocation(beaconGlassLocationString) : null;
        
        OccupationPoint point = new OccupationPoint(
                name, location, size, captureTime, recaptureTime, 
                beaconGlassLocation, textColor, barColor);
        
        // 버프 로드
        loadEffects(pointsSection, pointKey, point);
        
        // 디버프 로드
        loadDebuffs(pointsSection, pointKey, point);
        
        return point;
    }
    
    /**
     * 버프 효과 로드
     */
    private void loadEffects(ConfigurationSection pointsSection, String pointKey, OccupationPoint point) {
        ConfigurationSection effectsSection = pointsSection.getConfigurationSection(pointKey + ".effects");
        if (effectsSection != null) {
            for (String effectKey : effectsSection.getKeys(false)) {
                PotionEffectType type = PotionEffectType.getByName(effectKey.toUpperCase());
                if (type != null) {
                    int amplifier = effectsSection.getInt(effectKey + ".amplifier", 0);
                    point.addEffect(new PotionEffect(type, 40, amplifier, true, false));
                }
            }
        }
    }
    
    /**
     * 디버프 효과 로드
     */
    private void loadDebuffs(ConfigurationSection pointsSection, String pointKey, OccupationPoint point) {
        ConfigurationSection debuffsSection = pointsSection.getConfigurationSection(pointKey + ".debuffs");
        if (debuffsSection != null) {
            for (String debuffKey : debuffsSection.getKeys(false)) {
                PotionEffectType type = PotionEffectType.getByName(debuffKey.toUpperCase());
                if (type != null) {
                    int amplifier = debuffsSection.getInt(debuffKey + ".amplifier", 0);
                    point.addDebuff(new PotionEffect(type, 40, amplifier, true, false));
                }
            }
        }
    }
    
    /**
     * 위치 문자열을 Location 객체로 변환
     * @param locationString "world,x,y,z" 형식의 문자열
     * @return Location 객체, 파싱 실패시 null
     */
    private Location parseLocation(String locationString) {
        if (locationString == null) {
            return null;
        }
        
        String[] parts = locationString.split(",");
        if (parts.length != 4) {
            return null;
        }
        
        try {
            World world = Bukkit.getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            
            if (world == null) {
                plugin.getLogger().warning("월드를 찾을 수 없습니다: " + parts[0]);
                return null;
            }
            
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("잘못된 위치 형식입니다: " + locationString);
            return null;
        }
    }
    
    /**
     * 중앙 점령지 이름 로드
     * @return 중앙 점령지 이름
     */
    public String loadCenterPointName() {
        return plugin.getConfig().getString("center_point_name", "중앙");
    }
}

