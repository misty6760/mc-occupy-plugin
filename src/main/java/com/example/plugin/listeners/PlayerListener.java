package com.example.plugin.listeners;

import com.example.plugin.exchange.ExchangeManager;
import com.example.plugin.team.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 플레이어 이벤트 리스너
 * 교환 시스템과 팀 관리 관련 이벤트 처리
 */
public class PlayerListener implements Listener {
    private final TeamManager teamManager;
    private final ExchangeManager exchangeManager;

    public PlayerListener(TeamManager teamManager, ExchangeManager exchangeManager) {
        this.teamManager = teamManager;
        this.exchangeManager = exchangeManager;
    }

    /**
     * 플레이어 접속 이벤트
     * @param event 이벤트
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 환영 메시지
        player.sendMessage(ChatColor.GOLD + "=== 땅따먹기 게임에 오신 것을 환영합니다! ===");
        player.sendMessage(ChatColor.YELLOW + "게임 참여 방법:");
        player.sendMessage(ChatColor.GRAY + "1. /join <팀이름> - 팀에 가입");
        player.sendMessage(ChatColor.GRAY + "2. /info - 게임 정보 확인");
        player.sendMessage(ChatColor.GRAY + "3. /exchange - 교환 시스템 확인");
        
        // 현재 팀 상태 확인
        if (teamManager.hasTeam(player)) {
            String teamName = teamManager.getPlayerTeamName(player);
            player.sendMessage(ChatColor.GREEN + "현재 팀: " + teamName);
        } else {
            player.sendMessage(ChatColor.RED + "아직 팀에 가입하지 않았습니다!");
            player.sendMessage(ChatColor.YELLOW + "사용 가능한 팀: " + String.join(", ", teamManager.getTeamNames()));
        }
    }

    /**
     * 플레이어 퇴장 이벤트
     * @param event 이벤트
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 팀에서 제거 (선택사항 - 서버 재시작 시 팀 정보가 유지되도록 하려면 제거하지 않음)
        // Player player = event.getPlayer();
        // teamManager.removePlayerFromTeam(player);
    }

    /**
     * 플레이어 상호작용 이벤트 (교환 시스템)
     * @param event 이벤트
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        
        // 우클릭만 처리
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // 왼손 아이템 확인
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand == null || offhand.getType() == Material.AIR) {
            return;
        }
        
        // 교환 가능한 아이템인지 확인
        if (isExchangeableItem(offhand)) {
            // 교환 처리
            boolean success = exchangeManager.processExchange(player);
            
            if (success) {
                // 이벤트 취소 (아이템 사용 방지)
                event.setCancelled(true);
            }
        }
    }

    /**
     * 교환 가능한 아이템인지 확인
     * @param item 확인할 아이템
     * @return 교환 가능 여부
     */
    private boolean isExchangeableItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // 교환 가능한 아이템 타입들
        return item.getType() == Material.LAPIS_LAZULI || 
               item.getType() == Material.IRON_INGOT;
    }
}
