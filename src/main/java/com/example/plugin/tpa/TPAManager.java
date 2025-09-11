package com.example.plugin.tpa;

import com.example.plugin.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TPA (Teleport Ask) 시스템 관리자
 * 팀원들끼리만 사용할 수 있는 텔레포트 요청 시스템
 */
public class TPAManager {
    private final JavaPlugin plugin;
    private final TeamManager teamManager;
    private final Map<UUID, TPARequest> requests;
    private final Map<UUID, Long> cooldowns;
    
    // 설정값
    private static final int REQUEST_TIMEOUT = 30; // 30초
    private static final int COOLDOWN_TIME = 10; // 10초

    public TPAManager(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.requests = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    /**
     * TPA 요청 생성
     * @param requester 요청자
     * @param target 대상자
     * @return 요청 성공 여부
     */
    public boolean sendTPARequest(Player requester, Player target) {
        UUID requesterId = requester.getUniqueId();
        UUID targetId = target.getUniqueId();

        // 같은 플레이어인지 확인
        if (requesterId.equals(targetId)) {
            requester.sendMessage(ChatColor.RED + "자기 자신에게는 TPA 요청을 보낼 수 없습니다!");
            return false;
        }

        // 팀원인지 확인
        if (!teamManager.hasTeam(requester) || !teamManager.hasTeam(target)) {
            requester.sendMessage(ChatColor.RED + "TPA는 팀원들끼리만 사용할 수 있습니다!");
            return false;
        }

        String requesterTeam = teamManager.getPlayerTeamName(requester);
        String targetTeam = teamManager.getPlayerTeamName(target);
        
        if (!requesterTeam.equals(targetTeam)) {
            requester.sendMessage(ChatColor.RED + "같은 팀원에게만 TPA 요청을 보낼 수 있습니다!");
            return false;
        }

        // 쿨다운 확인
        if (cooldowns.containsKey(requesterId)) {
            long lastRequest = cooldowns.get(requesterId);
            long timeLeft = (lastRequest + COOLDOWN_TIME * 1000) - System.currentTimeMillis();
            if (timeLeft > 0) {
                requester.sendMessage(ChatColor.RED + "TPA 요청 쿨다운 중입니다! (" + (timeLeft / 1000) + "초 남음)");
                return false;
            }
        }

        // 기존 요청이 있는지 확인
        if (requests.containsKey(requesterId)) {
            requester.sendMessage(ChatColor.RED + "이미 TPA 요청을 보냈습니다! 기존 요청을 취소하거나 만료될 때까지 기다리세요.");
            return false;
        }

        // 대상자가 이미 요청을 받고 있는지 확인
        for (TPARequest request : requests.values()) {
            if (request.getTargetId().equals(targetId)) {
                requester.sendMessage(ChatColor.RED + target.getName() + "님이 이미 다른 TPA 요청을 받고 있습니다!");
                return false;
            }
        }

        // TPA 요청 생성
        TPARequest request = new TPARequest(requesterId, targetId, System.currentTimeMillis());
        requests.put(requesterId, request);

        // 쿨다운 설정
        cooldowns.put(requesterId, System.currentTimeMillis());

        // 메시지 전송
        requester.sendMessage(ChatColor.GREEN + target.getName() + "님에게 TPA 요청을 보냈습니다!");
        target.sendMessage(ChatColor.YELLOW + "📨 " + requester.getName() + "님이 TPA 요청을 보냈습니다!");
        target.sendMessage(ChatColor.GRAY + "수락: /tpaccept | 거부: /tpdeny");

        // 자동 만료 타이머 시작
        startRequestTimeout(requesterId);

        return true;
    }

    /**
     * TPA 요청 수락
     * @param target 수락자
     * @return 수락 성공 여부
     */
    public boolean acceptTPARequest(Player target) {
        UUID targetId = target.getUniqueId();

        // 요청 찾기
        TPARequest request = null;
        for (TPARequest req : requests.values()) {
            if (req.getTargetId().equals(targetId)) {
                request = req;
                break;
            }
        }

        if (request == null) {
            target.sendMessage(ChatColor.RED + "수락할 TPA 요청이 없습니다!");
            return false;
        }

        // 요청자 확인
        Player requester = Bukkit.getPlayer(request.getRequesterId());
        if (requester == null || !requester.isOnline()) {
            target.sendMessage(ChatColor.RED + "요청자가 오프라인입니다!");
            requests.remove(request.getRequesterId());
            return false;
        }

        // 팀원 확인 (요청 후 팀이 바뀌었을 수 있음)
        if (!teamManager.hasTeam(requester) || !teamManager.hasTeam(target)) {
            target.sendMessage(ChatColor.RED + "TPA는 팀원들끼리만 사용할 수 있습니다!");
            requests.remove(request.getRequesterId());
            return false;
        }

        String requesterTeam = teamManager.getPlayerTeamName(requester);
        String targetTeam = teamManager.getPlayerTeamName(target);
        
        if (!requesterTeam.equals(targetTeam)) {
            target.sendMessage(ChatColor.RED + "같은 팀원이 아니므로 TPA를 수락할 수 없습니다!");
            requests.remove(request.getRequesterId());
            return false;
        }

        // 텔레포트 실행
        requester.teleport(target.getLocation());
        
        // 요청 제거
        requests.remove(request.getRequesterId());

        // 메시지 전송
        requester.sendMessage(ChatColor.GREEN + target.getName() + "님이 TPA 요청을 수락했습니다!");
        target.sendMessage(ChatColor.GREEN + requester.getName() + "님의 TPA 요청을 수락했습니다!");

        // 효과음 재생
        requester.playSound(requester.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        target.playSound(target.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        return true;
    }

    /**
     * TPA 요청 거부
     * @param target 거부자
     * @return 거부 성공 여부
     */
    public boolean denyTPARequest(Player target) {
        UUID targetId = target.getUniqueId();

        // 요청 찾기
        TPARequest request = null;
        for (TPARequest req : requests.values()) {
            if (req.getTargetId().equals(targetId)) {
                request = req;
                break;
            }
        }

        if (request == null) {
            target.sendMessage(ChatColor.RED + "거부할 TPA 요청이 없습니다!");
            return false;
        }

        // 요청자 확인
        Player requester = Bukkit.getPlayer(request.getRequesterId());
        
        // 요청 제거
        requests.remove(request.getRequesterId());

        // 메시지 전송
        target.sendMessage(ChatColor.RED + "TPA 요청을 거부했습니다!");
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(ChatColor.RED + target.getName() + "님이 TPA 요청을 거부했습니다!");
        }

        return true;
    }

    /**
     * TPA 요청 취소
     * @param requester 취소자
     * @return 취소 성공 여부
     */
    public boolean cancelTPARequest(Player requester) {
        UUID requesterId = requester.getUniqueId();

        TPARequest request = requests.get(requesterId);
        if (request == null) {
            requester.sendMessage(ChatColor.RED + "취소할 TPA 요청이 없습니다!");
            return false;
        }

        // 요청 제거
        requests.remove(requesterId);

        // 대상자에게 알림
        Player target = Bukkit.getPlayer(request.getTargetId());
        if (target != null && target.isOnline()) {
            target.sendMessage(ChatColor.YELLOW + "📨 " + requester.getName() + "님이 TPA 요청을 취소했습니다!");
        }

        requester.sendMessage(ChatColor.YELLOW + "📨 TPA 요청을 취소했습니다!");
        return true;
    }

    /**
     * 요청 만료 타이머 시작
     * @param requesterId 요청자 UUID
     */
    private void startRequestTimeout(UUID requesterId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                TPARequest request = requests.get(requesterId);
                if (request != null) {
                    // 요청 제거
                    requests.remove(requesterId);

                    // 요청자에게 알림
                    Player requester = Bukkit.getPlayer(requesterId);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(ChatColor.RED + "⏰ TPA 요청이 만료되었습니다!");
                    }

                    // 대상자에게 알림
                    Player target = Bukkit.getPlayer(request.getTargetId());
                    if (target != null && target.isOnline()) {
                        target.sendMessage(ChatColor.RED + "⏰ TPA 요청이 만료되었습니다!");
                    }
                }
            }
        }.runTaskLater(plugin, REQUEST_TIMEOUT * 20L); // 30초 후 실행
    }

    /**
     * 플레이어의 TPA 요청 상태 확인
     * @param player 플레이어
     * @return 요청 상태 정보
     */
    public String getTPAStatus(Player player) {
        UUID playerId = player.getUniqueId();

        // 보낸 요청 확인
        TPARequest sentRequest = requests.get(playerId);
        if (sentRequest != null) {
            Player target = Bukkit.getPlayer(sentRequest.getTargetId());
            if (target != null) {
                long timeLeft = (sentRequest.getTimestamp() + REQUEST_TIMEOUT * 1000) - System.currentTimeMillis();
                return ChatColor.YELLOW + "보낸 요청: " + target.getName() + " (" + (timeLeft / 1000) + "초 남음)";
            }
        }

        // 받은 요청 확인
        for (TPARequest request : requests.values()) {
            if (request.getTargetId().equals(playerId)) {
                Player requester = Bukkit.getPlayer(request.getRequesterId());
                if (requester != null) {
                    long timeLeft = (request.getTimestamp() + REQUEST_TIMEOUT * 1000) - System.currentTimeMillis();
                    return ChatColor.GREEN + "받은 요청: " + requester.getName() + " (" + (timeLeft / 1000) + "초 남음)";
                }
            }
        }

        // 쿨다운 확인
        if (cooldowns.containsKey(playerId)) {
            long lastRequest = cooldowns.get(playerId);
            long timeLeft = (lastRequest + COOLDOWN_TIME * 1000) - System.currentTimeMillis();
            if (timeLeft > 0) {
                return ChatColor.RED + "쿨다운 중 (" + (timeLeft / 1000) + "초 남음)";
            }
        }

        return ChatColor.GRAY + "TPA 요청 없음";
    }

    /**
     * 모든 TPA 요청 초기화
     */
    public void clearAllRequests() {
        requests.clear();
        cooldowns.clear();
    }
}
