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

        // 웅크리기 상태가 아니면 무시
        if (!player.isSneaking()) {
            return;
        }

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
        ItemStack offHandItem = inventory.getItemInOffHand();

        // 왼손 아이템 확인
        if (offHandItem == null || offHandItem.getType() != recipe.getInput()) {
            player.sendMessage(ChatColor.RED + "왼손에 교환할 아이템을 들고 있어야 합니다!");
            return false;
        }

        int offHandAmount = offHandItem.getAmount();

        // 왼손에 충분한 아이템이 있는지 확인
        if (offHandAmount < recipe.getInputAmount()) {
            player.sendMessage(ChatColor.RED + "왼손에 아이템이 부족합니다! (필요: " + recipe.getInputAmount() + "개, 현재: "
                    + offHandAmount + "개)");
            return false;
        }

        // 인벤토리에 공간이 있는지 확인 (왼손 제외)
        if (!hasInventorySpaceExcludingOffHand(inventory, recipe.getOutput(), recipe.getOutputAmount())) {
            player.sendMessage(ChatColor.RED + "인벤토리에 공간이 부족합니다!");
            return false;
        }

        // 왼손 아이템 제거
        int remainingInOffHand = offHandAmount - recipe.getInputAmount();
        if (remainingInOffHand > 0) {
            offHandItem.setAmount(remainingInOffHand);
        } else {
            inventory.setItemInOffHand(null);
        }

        // 결과물 생성
        ItemStack output = new ItemStack(recipe.getOutput(), recipe.getOutputAmount());

        // 결과물을 인벤토리에 추가 (왼손은 비어있거나 남은 아이템이 있으므로 인벤토리에 추가)
        inventory.addItem(output);

        // 한글 메시지 출력
        String itemName;
        if (recipe.getInput() == Material.LAPIS_LAZULI) {
            itemName = "청금석";
        } else if (recipe.getInput() == Material.IRON_INGOT) {
            itemName = "철";
        } else {
            itemName = recipe.getInput().name();
        }

        player.sendMessage(ChatColor.GREEN + itemName + " 교환 완료!");

        return true;
    }

    /**
     * 인벤토리에 공간이 있는지 확인 (왼손 제외)
     */
    private boolean hasInventorySpaceExcludingOffHand(PlayerInventory inventory, Material material, int amount) {
        int maxStackSize = material.getMaxStackSize();
        int remainingAmount = amount;

        // 기존 스택에 추가 가능한지 확인 (왼손 제외)
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            // 왼손 슬롯(40번)은 제외
            if (i == 40) {
                continue;
            }

            ItemStack item = contents[i];
            if (item != null && item.getType() == material && item.getAmount() < maxStackSize) {
                remainingAmount -= (maxStackSize - item.getAmount());
                if (remainingAmount <= 0) {
                    return true;
                }
            }
        }

        // 빈 슬롯 확인 (왼손 제외)
        int emptySlots = 0;
        for (int i = 0; i < contents.length; i++) {
            // 왼손 슬롯(40번)은 제외
            if (i == 40) {
                continue;
            }

            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        int requiredSlots = (int) Math.ceil((double) remainingAmount / maxStackSize);
        return emptySlots >= requiredSlots;
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
