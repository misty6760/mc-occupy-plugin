package org.think_ing.occupyplugin.game;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class OccupationPoint {

    private final String name;
    private final Location location;
    private final double size;
    private final int captureTime;
    private final int recaptureTime;
    private final Location beaconGlassLocation;
    private final ChatColor textColor;
    private final BarColor barColor;
    private Team owner;
    private double captureProgress;
    private final List<Player> playersInZone;
    private final List<PotionEffect> effects = new ArrayList<>();
    private final List<PotionEffect> debuffs = new ArrayList<>();
    
    // 테스트 모드
    private int testCaptureTime;
    private int testRecaptureTime;
    private boolean isTestMode = false;


    public OccupationPoint(String name, Location location, double size, int captureTime, int recaptureTime, Location beaconGlassLocation, ChatColor textColor, BarColor barColor) {
        this.name = name;
        this.location = location;
        this.size = size;
        this.captureTime = captureTime;
        this.recaptureTime = recaptureTime;
        this.beaconGlassLocation = beaconGlassLocation;
        this.textColor = textColor;
        this.barColor = barColor;
        this.owner = null;
        this.captureProgress = 0.0;
        this.playersInZone = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public double getSize() {
        return size;
    }

    public int getCaptureTime() {
        return isTestMode ? testCaptureTime : captureTime;
    }

    public int getRecaptureTime() {
        return isTestMode ? testRecaptureTime : recaptureTime;
    }
    
    /**
     * 테스트 모드 활성화
     */
    public void setTestMode(boolean testMode, int testCaptureTime, int testRecaptureTime) {
        this.isTestMode = testMode;
        this.testCaptureTime = testCaptureTime;
        this.testRecaptureTime = testRecaptureTime;
    }
    
    /**
     * 테스트 모드 확인
     */
    public boolean isTestMode() {
        return isTestMode;
    }

    public Location getBeaconGlassLocation() {
        return beaconGlassLocation;
    }

    public ChatColor getTextColor() {
        return textColor;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public Team getOwner() {
        return owner;
    }

    public void setOwner(Team owner) {
        this.owner = owner;
    }

    public double getCaptureProgress() {
        return captureProgress;
    }

    public void setCaptureProgress(double captureProgress) {
        this.captureProgress = captureProgress;
    }

    public List<Player> getPlayersInZone() {
        return playersInZone;
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }

    public void addEffect(PotionEffect effect) {
        effects.add(effect);
    }

    public List<PotionEffect> getDebuffs() {
        return debuffs;
    }

    public void addDebuff(PotionEffect debuff) {
        debuffs.add(debuff);
    }
}
