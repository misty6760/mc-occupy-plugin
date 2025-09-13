package com.example.plugin.commands;

import com.example.plugin.exchange.ExchangeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 교환 관련 명령어 처리
 * /exchange - 교환 가능한 아이템 목록 표시
 * /exchange info - 플레이어의 교환 가능한 아이템 정보 표시
 */
public class ExchangeCommand implements CommandExecutor, TabCompleter {
    private final ExchangeManager exchangeManager;

    public ExchangeCommand(ExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // 교환 가능한 아이템 목록 표시
            exchangeManager.showExchangeList(player);
        } else if (args.length == 1) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "info":
                    // 플레이어의 교환 가능한 아이템 정보 표시
                    String info = exchangeManager.getPlayerExchangeInfo(player);
                    player.sendMessage(info);
                    break;
                case "help":
                    showExchangeHelp(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다!");
                    showExchangeHelp(player);
                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "사용법: /exchange [info|help]");
        }

        return true;
    }

    /**
     * 교환 명령어 도움말 표시
     * @param player 대상 플레이어
     */
    private void showExchangeHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 교환 명령어 도움말 ===");
        player.sendMessage(ChatColor.YELLOW + "/exchange - 교환 가능한 아이템 목록 표시");
        player.sendMessage(ChatColor.YELLOW + "/exchange info - 내가 교환할 수 있는 아이템 확인");
        player.sendMessage(ChatColor.YELLOW + "/exchange help - 이 도움말 표시");
        player.sendMessage(ChatColor.GRAY + "");
        player.sendMessage(ChatColor.GRAY + "교환 방법:");
        player.sendMessage(ChatColor.GRAY + "1. 교환할 아이템을 왼손에 들기");
        player.sendMessage(ChatColor.GRAY + "2. 아이템을 우클릭하여 교환 실행");
        player.sendMessage(ChatColor.GRAY + "3. 교환된 아이템은 인벤토리에 추가됨");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 첫 번째 인수: exchange 하위 명령어
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            if ("info".startsWith(input)) {
                completions.add("info");
            }
            if ("help".startsWith(input)) {
                completions.add("help");
            }
            
            return completions;
        }
        
        return Collections.emptyList();
    }
}
