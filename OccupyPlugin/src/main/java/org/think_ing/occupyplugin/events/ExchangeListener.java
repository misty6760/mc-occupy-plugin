package org.think_ing.occupyplugin.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.think_ing.occupyplugin.OccupyPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 교환 시스템 리스너
 * 플레이어가 F키(왼손 들기)를 눌렀을 때 아이템 교환을 처리합니다
 * 
 * 교환 규칙:
 * - 청금석 64개 → 경험치 병 64개
 * - 철 32개 → 빵 64개
 */
public class ExchangeListener implements Listener {

    private final OccupyPlugin plugin;
    private final List<ExchangeRecipe> recipes;

    public ExchangeListener(OccupyPlugin plugin) {
        this.plugin = plugin;
        this.recipes = loadRecipes();
    }

    /**
     * config.yml에서 교환 레시피 로드
     */
    private List<ExchangeRecipe> loadRecipes() {
        List<ExchangeRecipe> recipeList = new ArrayList<>();

        if (!plugin.getConfig().getBoolean("exchange.enabled", true)) {
            plugin.getLogger().info("교환 시스템이 비활성화되어 있습니다.");
            return recipeList;
        }

        List<?> exchangesList = plugin.getConfig().getList("exchange.exchanges");
        if (exchangesList == null) {
            plugin.getLogger().warning("교환 레시피를 찾을 수 없습니다!");
            return recipeList;
        }

        for (Object obj : exchangesList) {
            try {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;
                Material input = Material.valueOf((String) map.get("input"));
                int inputAmount = ((Number) map.get("input_amount")).intValue();
                Material output = Material.valueOf((String) map.get("output"));
                int outputAmount = ((Number) map.get("output_amount")).intValue();

                recipeList.add(new ExchangeRecipe(input, inputAmount, output, outputAmount));
                plugin.getLogger()
                        .info("교환 레시피 로드: " + input + " x" + inputAmount + " → " + output + " x" + outputAmount);
            } catch (Exception e) {
                plugin.getLogger().warning("교환 레시피 로드 실패: " + e.getMessage());
            }
        }

        plugin.getLogger().info("총 " + recipeList.size() + "개의 교환 레시피가 로드되었습니다.");
        return recipeList;
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack offHandItem = inventory.getItemInOffHand();

        // 왼손(오프핸드)에 아이템이 없으면 무시
        if (offHandItem == null || offHandItem.getType() == Material.AIR) {
            return;
        }

        // 교환 레시피가 없으면 무시
        if (recipes.isEmpty()) {
            return;
        }

        // 교환 레시피 확인
        for (ExchangeRecipe recipe : recipes) {
            if (recipe.matches(offHandItem)) {
                // 교환 시도
                if (tryExchange(player, inventory, recipe)) {
                    event.setCancelled(true); // F키 본래 기능 취소
                    return;
                }
            }
        }
    }

    /**
     * 교환 실행
     * 
     * @return 교환 성공 여부
     */
    private boolean tryExchange(Player player, PlayerInventory inventory, ExchangeRecipe recipe) {
        // 필요한 아이템 개수 확인
        if (!hasEnoughItems(inventory, recipe.getInput(), recipe.getInputAmount())) {
            player.sendMessage(ChatColor.RED + "교환에 필요한 아이템이 부족합니다: "
                    + recipe.getInput().name() + " x" + recipe.getInputAmount());
            return false;
        }

        // 인벤토리에 공간이 있는지 확인
        if (!hasInventorySpace(inventory, recipe.getOutput(), recipe.getOutputAmount())) {
            player.sendMessage(ChatColor.RED + "인벤토리에 공간이 부족합니다!");
            return false;
        }

        // 아이템 제거
        removeItems(inventory, recipe.getInput(), recipe.getInputAmount());

        // 아이템 추가
        ItemStack output = new ItemStack(recipe.getOutput(), recipe.getOutputAmount());
        inventory.addItem(output);

        player.sendMessage(ChatColor.GREEN + "교환 완료! "
                + recipe.getInput().name() + " x" + recipe.getInputAmount()
                + " → " + recipe.getOutput().name() + " x" + recipe.getOutputAmount());

        return true;
    }

    /**
     * 인벤토리에 충분한 아이템이 있는지 확인
     */
    private boolean hasEnoughItems(PlayerInventory inventory, Material material, int amount) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    /**
     * 인벤토리에 공간이 있는지 확인
     */
    private boolean hasInventorySpace(PlayerInventory inventory, Material material, int amount) {
        int maxStackSize = material.getMaxStackSize();
        int remainingAmount = amount;

        // 기존 스택에 추가 가능한지 확인
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material && item.getAmount() < maxStackSize) {
                remainingAmount -= (maxStackSize - item.getAmount());
                if (remainingAmount <= 0) {
                    return true;
                }
            }
        }

        // 빈 슬롯 확인
        int emptySlots = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        int requiredSlots = (int) Math.ceil((double) remainingAmount / maxStackSize);
        return emptySlots >= requiredSlots;
    }

    /**
     * 인벤토리에서 아이템 제거
     */
    private void removeItems(PlayerInventory inventory, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    item.setAmount(0);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }

                if (remaining == 0) {
                    break;
                }
            }
        }
    }

    /**
     * 교환 레시피 클래스
     */
    private static class ExchangeRecipe {
        private final Material input;
        private final int inputAmount;
        private final Material output;
        private final int outputAmount;

        public ExchangeRecipe(Material input, int inputAmount, Material output, int outputAmount) {
            this.input = input;
            this.inputAmount = inputAmount;
            this.output = output;
            this.outputAmount = outputAmount;
        }

        public boolean matches(ItemStack item) {
            return item != null && item.getType() == input;
        }

        public Material getInput() {
            return input;
        }

        public int getInputAmount() {
            return inputAmount;
        }

        public Material getOutput() {
            return output;
        }

        public int getOutputAmount() {
            return outputAmount;
        }
    }
}
