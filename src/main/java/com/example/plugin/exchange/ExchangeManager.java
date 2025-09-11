package com.example.plugin.exchange;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * 교환 시스템 관리자 클래스
 * 철/청금석 교환 기능을 관리
 */
public class ExchangeManager {
    
    /**
     * 교환 가능한 아이템 타입
     */
    public enum ExchangeType {
        LAPIS_TO_XP("청금석 64개", Material.LAPIS_LAZULI, 64, 
        "경험치 병 64개", Material.EXPERIENCE_BOTTLE, 64),
        IRON_TO_BREAD("철 32개", Material.IRON_INGOT, 32, 
        "빵 64개", Material.BREAD, 64);
        
        private final String inputName;
        private final Material inputMaterial;
        private final int inputAmount;
        private final String outputName;
        private final Material outputMaterial;
        private final int outputAmount;
        
        ExchangeType(String inputName, Material inputMaterial, int inputAmount,
                    String outputName, Material outputMaterial, int outputAmount) {
            this.inputName = inputName;
            this.inputMaterial = inputMaterial;
            this.inputAmount = inputAmount;
            this.outputName = outputName;
            this.outputMaterial = outputMaterial;
            this.outputAmount = outputAmount;
        }
        
        public String getInputName() { return inputName; }
        public Material getInputMaterial() { return inputMaterial; }
        public int getInputAmount() { return inputAmount; }
        public String getOutputName() { return outputName; }
        public Material getOutputMaterial() { return outputMaterial; }
        public int getOutputAmount() { return outputAmount; }
    }

    /**
     * 플레이어가 왼손에 들고 있는 아이템으로 교환 처리
     * @param player 교환할 플레이어
     * @return 교환 성공 여부
     */
    public boolean processExchange(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack offhand = inventory.getItemInOffHand();
        
        if (offhand == null || offhand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "왼손에 교환할 아이템을 들고 있어야 합니다!");
            return false;
        }
        
        // 교환 타입 확인
        ExchangeType exchangeType = getExchangeType(offhand);
        if (exchangeType == null) {
            player.sendMessage(ChatColor.RED + "교환할 수 없는 아이템입니다!");
            player.sendMessage(ChatColor.YELLOW + "교환 가능한 아이템:");
            for (ExchangeType type : ExchangeType.values()) {
                player.sendMessage(ChatColor.GRAY + "- " + type.getInputName() + " → " + type.getOutputName());
            }
            return false;
        }
        
        // 아이템 수량 확인
        if (offhand.getAmount() < exchangeType.getInputAmount()) {
            player.sendMessage(ChatColor.RED + "아이템이 부족합니다! (" + 
                             exchangeType.getInputName() + " 필요)");
            return false;
        }
        
        // 인벤토리 공간 확인
        if (!hasSpaceForItem(inventory, exchangeType.getOutputMaterial(), exchangeType.getOutputAmount())) {
            player.sendMessage(ChatColor.RED + "인벤토리 공간이 부족합니다!");
            return false;
        }
        
        // 교환 실행
        return executeExchange(player, exchangeType, offhand);
    }

    /**
     * 아이템으로 교환 타입 확인
     * @param item 확인할 아이템
     * @return 교환 타입, 없으면 null
     */
    private ExchangeType getExchangeType(ItemStack item) {
        for (ExchangeType type : ExchangeType.values()) {
            if (item.getType() == type.getInputMaterial()) {
                return type;
            }
        }
        return null;
    }

    /**
     * 인벤토리에 아이템을 넣을 공간이 있는지 확인
     * @param inventory 플레이어 인벤토리
     * @param material 확인할 아이템 타입
     * @param amount 확인할 수량
     * @return 공간 여부
     */
    private boolean hasSpaceForItem(PlayerInventory inventory, Material material, int amount) {
        int availableSpace = 0;
        
        // 빈 슬롯 확인
        for (int i = 0; i < 36; i++) { // 인벤토리 슬롯만 확인
            ItemStack slot = inventory.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                availableSpace += 64; // 빈 슬롯은 64개까지 가능
            } else if (slot.getType() == material && slot.getAmount() < 64) {
                availableSpace += 64 - slot.getAmount(); // 같은 아이템이 있으면 남은 공간
            }
        }
        
        return availableSpace >= amount;
    }

    /**
     * 교환 실행
     * @param player 교환할 플레이어
     * @param exchangeType 교환 타입
     * @param offhand 왼손 아이템
     * @return 교환 성공 여부
     */
    private boolean executeExchange(Player player, ExchangeType exchangeType, ItemStack offhand) {
        PlayerInventory inventory = player.getInventory();
        
        // 왼손에서 아이템 제거
        int remainingAmount = offhand.getAmount() - exchangeType.getInputAmount();
        if (remainingAmount > 0) {
            offhand.setAmount(remainingAmount);
        } else {
            inventory.setItemInOffHand(null);
        }
        
        // 교환 아이템 지급
        ItemStack exchangeItem = new ItemStack(exchangeType.getOutputMaterial(), exchangeType.getOutputAmount());
        inventory.addItem(exchangeItem);
        
        // 성공 메시지
        player.sendMessage(ChatColor.GREEN + "교환 완료!");
        player.sendMessage(ChatColor.YELLOW + exchangeType.getInputName() + " → " + exchangeType.getOutputName());
        
        // 효과음 재생
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        
        return true;
    }

    /**
     * 교환 가능한 아이템 목록을 플레이어에게 표시
     * @param player 대상 플레이어
     */
    public void showExchangeList(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 교환 가능한 아이템 ===");
        for (ExchangeType type : ExchangeType.values()) {
            player.sendMessage(ChatColor.YELLOW + "• " + type.getInputName() + 
                             ChatColor.WHITE + " → " + 
                             ChatColor.GREEN + type.getOutputName());
        }
        player.sendMessage(ChatColor.GRAY + "왼손에 교환할 아이템을 들고 우클릭하세요!");
    }

    /**
     * 플레이어의 교환 가능한 아이템 수량 확인
     * @param player 확인할 플레이어
     * @return 교환 가능한 아이템 정보
     */
    public String getPlayerExchangeInfo(Player player) {
        PlayerInventory inventory = player.getInventory();
        StringBuilder info = new StringBuilder();
        
        info.append(ChatColor.GOLD).append("=== 교환 가능한 아이템 ===\n");
        
        for (ExchangeType type : ExchangeType.values()) {
            int amount = getItemAmount(inventory, type.getInputMaterial());
            if (amount >= type.getInputAmount()) {
                int possibleExchanges = amount / type.getInputAmount();
                info.append(ChatColor.GREEN).append("✓ ").append(type.getInputName())
                    .append(ChatColor.WHITE).append(" (").append(amount).append("개) → ")
                    .append(ChatColor.YELLOW).append(possibleExchanges).append("회 교환 가능\n");
            } else {
                info.append(ChatColor.RED).append("✗ ").append(type.getInputName())
                    .append(ChatColor.WHITE).append(" (").append(amount).append("/").append(type.getInputAmount()).append(")\n");
            }
        }
        
        return info.toString();
    }

    /**
     * 인벤토리에서 특정 아이템의 총 수량 확인
     * @param inventory 플레이어 인벤토리
     * @param material 확인할 아이템 타입
     * @return 총 수량
     */
    private int getItemAmount(PlayerInventory inventory, Material material) {
        int totalAmount = 0;
        
        for (int i = 0; i < 36; i++) { // 인벤토리 슬롯만 확인
            ItemStack slot = inventory.getItem(i);
            if (slot != null && slot.getType() == material) {
                totalAmount += slot.getAmount();
            }
        }
        
        // 왼손 아이템도 확인
        ItemStack offhand = inventory.getItemInOffHand();
        if (offhand != null && offhand.getType() == material) {
            totalAmount += offhand.getAmount();
        }
        
        return totalAmount;
    }
}
