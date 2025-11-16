package org.think_ing.occupyplugin.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.think_ing.occupyplugin.OccupyPlugin;
import org.think_ing.occupyplugin.display.NotificationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 점령 시스템
 * 점령지 점령/재점령 로직을 처리합니다
 */
public class CaptureSystem {

    private final OccupyPlugin plugin;
    private final NotificationManager notificationManager;
    private final BeaconManager beaconManager;
    private final String centerPointName;

    // 점령 진행 알림을 위한 마지막 알림 진행도 저장
    private final Map<OccupationPoint, Integer> lastNotifiedProgress = new HashMap<>();
    // 기존 소유자에게 뺏기는 중 알림을 보냈는지 여부 (중복 알림 방지)
    private final Map<OccupationPoint, Boolean> notifiedUnderAttack = new HashMap<>();

    public CaptureSystem(OccupyPlugin plugin, NotificationManager notificationManager, String centerPointName) {
        this.plugin = plugin;
        this.notificationManager = notificationManager;
        this.beaconManager = new BeaconManager();
        this.centerPointName = centerPointName;
    }

    /**
     * 모든 점령지 업데이트
     * 
     * @param occupationPoints       점령지 리스트
     * @param participatingTeamNames 참여 팀 이름 리스트
     */
    public void updateOccupationPoints(List<OccupationPoint> occupationPoints, List<String> participatingTeamNames) {
        for (OccupationPoint point : occupationPoints) {
            updateSinglePoint(point, occupationPoints, participatingTeamNames);
        }
    }

    /**
     * 개별 점령지 업데이트
     */
    private void updateSinglePoint(OccupationPoint point, List<OccupationPoint> allPoints,
            List<String> participatingTeamNames) {
        point.getPlayersInZone().clear();
        List<Player> playersInZone = getPlayersInPoint(point);
        point.getPlayersInZone().addAll(playersInZone);

        Map<Team, List<Player>> teamsInZone = getTeamsInZone(playersInZone, participatingTeamNames);

        if (teamsInZone.size() == 1) {
            handleSingleTeamCapture(point, allPoints, teamsInZone);
        } else {
            handleMultipleTeamsOrEmpty(point, teamsInZone, playersInZone);
        }
    }

    /**
     * 단일 팀이 점령지에 있을 때의 처리
     */
    private void handleSingleTeamCapture(OccupationPoint point, List<OccupationPoint> allPoints,
            Map<Team, List<Player>> teamsInZone) {
        Map.Entry<Team, List<Player>> entry = teamsInZone.entrySet().iterator().next();
        Team capturingTeam = entry.getKey();
        int playerCount = entry.getValue().size();

        // 중앙 점령지 특수 규칙: 한 팀이 기본 점령지 3개를 점령했으면 중앙 점령 불가
        if (point.getName().equals(centerPointName)) {
            if (isThreeBasePointsRule(allPoints, capturingTeam)) {
                // 중앙 점령 차단
                point.setCaptureProgress(0);
                for (Player player : entry.getValue()) {
                    notificationManager.sendTitle(player, ChatColor.RED + "점령 불가",
                            ChatColor.YELLOW + "기본 점령지 3개 점령 시 중앙 점령 불가!", 0, 40, 10);
                }
                return;
            }
        }

        if (point.getOwner() == null) {
            // 무주지 점령
            captureNeutralPoint(point, capturingTeam, playerCount);
        } else if (!point.getOwner().equals(capturingTeam)) {
            // 재점령 (탈환)
            recapturePoint(point, capturingTeam, playerCount);
        } else {
            // 이미 점령한 팀이 있음
            point.setCaptureProgress(0);
            lastNotifiedProgress.remove(point);
        }
    }

    /**
     * 3개 기본 점령지 규칙 확인
     * 
     * @param allPoints 모든 점령지
     * @param team      확인할 팀
     * @return 기본 점령지 3개 이상 점령했으면 true
     */
    private boolean isThreeBasePointsRule(List<OccupationPoint> allPoints, Team team) {
        long ownedBasePoints = allPoints.stream()
                .filter(p -> !p.getName().equals(centerPointName))
                .filter(p -> team.equals(p.getOwner()))
                .count();

        return ownedBasePoints >= 3;
    }

    /**
     * 무주지 점령 처리
     */
    private void captureNeutralPoint(OccupationPoint point, Team capturingTeam, int playerCount) {
        double oldProgress = point.getCaptureProgress();
        double progressIncrease = playerCount;
        point.setCaptureProgress(oldProgress + progressIncrease);

        // 중앙 점령지만 진행 중 알림 (25%, 50%, 75%)
        if (point.getName().equals(centerPointName)) {
            sendCaptureProgressNotification(point, capturingTeam, oldProgress, false);
        }

        if (point.getCaptureProgress() >= point.getCaptureTime()) {
            setPointOwner(point, capturingTeam, false);
        }
    }

    /**
     * 재점령 처리 (탈환)
     */
    private void recapturePoint(OccupationPoint point, Team capturingTeam, int playerCount) {
        Team originalOwner = point.getOwner();
        double oldProgress = point.getCaptureProgress();
        double progressIncrease = playerCount;
        point.setCaptureProgress(oldProgress + progressIncrease);

        // 기존 소유자 팀에게 뺏기는 중 알림 (한 번만)
        if (originalOwner != null && !notifiedUnderAttack.getOrDefault(point, false)) {
            notifyOriginalOwnerUnderAttack(point, originalOwner, capturingTeam);
            notifiedUnderAttack.put(point, true);
        }

        // 중앙 점령지만 재점령 진행 중 알림 (25%, 50%, 75%)
        if (point.getName().equals(centerPointName)) {
            sendCaptureProgressNotification(point, capturingTeam, oldProgress, true);
        }

        if (point.getCaptureProgress() >= point.getRecaptureTime()) {
            setPointOwner(point, capturingTeam, true);
        }
    }

    /**
     * 기존 소유자 팀에게 점령지가 공격받고 있음을 알림
     */
    private void notifyOriginalOwnerUnderAttack(OccupationPoint point, Team originalOwner, Team attackingTeam) {
        String pointName = point.getName();
        @SuppressWarnings("deprecation")
        String attackingTeamName = attackingTeam.getDisplayName();
        @SuppressWarnings("deprecation")
        ChatColor attackingTeamColor = attackingTeam.getColor();

        // 기존 소유자 팀의 모든 플레이어에게 알림
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team playerTeam = getPlayerTeam(player);
            if (playerTeam != null && playerTeam.equals(originalOwner)) {
                notificationManager.sendTitle(player,
                        ChatColor.RED + "점령지 위험!",
                        ChatColor.WHITE + pointName + "이(가) " + attackingTeamColor + attackingTeamName + ChatColor.WHITE
                                + " 팀에게 뺏기는 중입니다!",
                        0, 40, 10);
            }
        }
    }

    /**
     * 점령 진행 중 알림 발송 (중앙 점령지 전용)
     * 
     * @param point         점령지
     * @param capturingTeam 점령하는 팀
     * @param oldProgress   이전 진행도
     * @param isRecapture   재점령 여부
     */
    private void sendCaptureProgressNotification(OccupationPoint point, Team capturingTeam,
            double oldProgress, boolean isRecapture) {
        double targetTime = isRecapture ? point.getRecaptureTime() : point.getCaptureTime();
        double currentProgress = point.getCaptureProgress();

        int oldPercent = (int) ((oldProgress / targetTime) * 100);
        int currentPercent = (int) ((currentProgress / targetTime) * 100);

        // 25% 단위로 알림 (중앙 점령지만)
        int[] milestones = { 25, 50, 75 };
        for (int milestone : milestones) {
            if (oldPercent < milestone && currentPercent >= milestone) {
                @SuppressWarnings("deprecation")
                String teamName = capturingTeam.getDisplayName();
                @SuppressWarnings("deprecation")
                ChatColor teamColor = capturingTeam.getColor();

                String action = isRecapture ? "탈환" : "점령";
                String pointName = point.getName();

                // 중앙 점령지는 전체 공지
                notificationManager.broadcastTitle(
                        teamColor + teamName + " 팀",
                        ChatColor.WHITE + pointName + "을(를) " + action + " 중입니다! (" + milestone + "%)",
                        5, 30, 5);

                lastNotifiedProgress.put(point, milestone);
                break;
            }
        }
    }

    /**
     * 여러 팀이 있거나 아무도 없을 때의 처리
     */
    private void handleMultipleTeamsOrEmpty(OccupationPoint point, Map<Team, List<Player>> teamsInZone,
            List<Player> playersInZone) {
        // 고착 상태일 때는 점령 진척도를 초기화하지 않음 (진행도 유지)
        // 아무도 없을 때만 초기화
        if (teamsInZone.isEmpty()) {
            point.setCaptureProgress(0);
            lastNotifiedProgress.remove(point);
            notifiedUnderAttack.remove(point); // 아무도 없을 때 알림 상태 초기화
        }
        // 여러 팀이 있을 때는 진행도 유지 (고착 상태)
        else if (teamsInZone.size() > 1) {
            // 여러 팀이 동시에 있으면 경고 (진행도는 유지)
            for (Player p : playersInZone) {
                notificationManager.sendTitle(p, ChatColor.RED + "경고",
                        "점령지에 다른 팀이 있습니다!", 0, 25, 5);
            }
        }
    }

    /**
     * 점령지의 소유자 설정
     * 
     * @param point       점령지
     * @param newOwner    새 소유자
     * @param isRecapture 재점령 여부
     */
    private void setPointOwner(OccupationPoint point, Team newOwner, boolean isRecapture) {
        point.setOwner(newOwner);
        point.setCaptureProgress(0);
        lastNotifiedProgress.remove(point);
        notifiedUnderAttack.remove(point); // 점령 완료 시 알림 상태 초기화

        @SuppressWarnings("deprecation")
        String ownerName = newOwner.getDisplayName();
        String pointName = point.getName();

        @SuppressWarnings("deprecation")
        ChatColor ownerColor = newOwner.getColor();

        String action = isRecapture ? "탈환했습니다" : "점령했습니다";

        notificationManager.broadcastTitle(
                ownerColor + ownerName + " 팀",
                ChatColor.WHITE + pointName + "을(를) " + action + "!",
                10, 40, 10);

        // 비콘 색상 변경
        if (point.getBeaconGlassLocation() != null) {
            Block beaconGlass = point.getBeaconGlassLocation().getBlock();
            @SuppressWarnings("deprecation")
            Material stainedGlass = beaconManager.getStainedGlass(newOwner.getColor());
            beaconGlass.setType(stainedGlass);
        }
    }

    /**
     * 점령지 내의 팀별 플레이어 그룹 생성
     */
    private Map<Team, List<Player>> getTeamsInZone(List<Player> playersInZone, List<String> participatingTeamNames) {
        return playersInZone.stream()
                .filter(p -> {
                    Team team = getPlayerTeam(p);
                    return team != null && participatingTeamNames != null
                            && participatingTeamNames.contains(team.getName());
                })
                .collect(Collectors.groupingBy(this::getPlayerTeam));
    }

    /**
     * 점령지 내의 모든 플레이어 조회
     */
    private List<Player> getPlayersInPoint(OccupationPoint point) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerInPoint(player, point)) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * 플레이어가 점령지 내에 있는지 확인
     */
    public boolean isPlayerInPoint(Player player, OccupationPoint point) {
        if (!player.getWorld().equals(point.getLocation().getWorld())) {
            return false;
        }

        Location playerLoc = player.getLocation();
        Location pointLoc = point.getLocation();
        double halfSize = point.getSize() / 2.0;

        return Math.abs(playerLoc.getX() - pointLoc.getX()) <= halfSize &&
                Math.abs(playerLoc.getZ() - pointLoc.getZ()) <= halfSize;
    }

    /**
     * 플레이어의 팀 조회
     */
    public Team getPlayerTeam(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        return scoreboard.getEntryTeam(player.getName());
    }

    /**
     * 모든 점령지 초기화
     */
    public void resetPoints(List<OccupationPoint> occupationPoints) {
        for (OccupationPoint point : occupationPoints) {
            point.setOwner(null);
            point.setCaptureProgress(0);
            point.getPlayersInZone().clear();
        }
        lastNotifiedProgress.clear();
        notifiedUnderAttack.clear();
    }
}
