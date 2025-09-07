package com.example.plugin.commands;

import com.example.plugin.tpa.TPAManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * TPA 명령어 처리
 * /tpa <플레이어> - TPA 요청 보내기
 * /tpaccept - TPA 요청 수락
 * /tpdeny - TPA 요청 거부
 * /tpcancel - TPA 요청 취소
 * /tpastatus - TPA 상태 확인
 */
public class TPACommand implements CommandExecutor, TabCompleter {
    private final TPAManager tpaManager;

    public TPACommand(TPAManager tpaManager) {
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "tpa":
                handleTPA(player, args);
                break;
            case "tpaccept":
                handleTPAccept(player);
                break;
            case "tpdeny":
                handleTPDeny(player);
                break;
            case "tpcancel":
                handleTPCancel(player);
                break;
            case "tpastatus":
                handleTPAStatus(player);
                break;
            default:
                showTPAHelp(player);
                break;
        }

        return true;
    }

    /**
     * TPA 요청 처리
     * @param player 명령어 실행자
     * @param args 명령어 인수
     */
    private void handleTPA(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "사용법: /tpa <플레이어>");
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + targetName);
            return;
        }

        if (target == player) {
            player.sendMessage(ChatColor.RED + "자기 자신에게는 TPA 요청을 보낼 수 없습니다!");
            return;
        }

        tpaManager.sendTPARequest(player, target);
    }

    /**
     * TPA 수락 처리
     * @param player 명령어 실행자
     */
    private void handleTPAccept(Player player) {
        tpaManager.acceptTPARequest(player);
    }

    /**
     * TPA 거부 처리
     * @param player 명령어 실행자
     */
    private void handleTPDeny(Player player) {
        tpaManager.denyTPARequest(player);
    }

    /**
     * TPA 취소 처리
     * @param player 명령어 실행자
     */
    private void handleTPCancel(Player player) {
        tpaManager.cancelTPARequest(player);
    }

    /**
     * TPA 상태 확인 처리
     * @param player 명령어 실행자
     */
    private void handleTPAStatus(Player player) {
        String status = tpaManager.getTPAStatus(player);
        player.sendMessage(ChatColor.GOLD + "=== TPA 상태 ===");
        player.sendMessage(status);
    }

    /**
     * TPA 도움말 표시
     * @param player 대상 플레이어
     */
    private void showTPAHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== TPA 명령어 도움말 ===");
        player.sendMessage(ChatColor.YELLOW + "/tpa <플레이어> - TPA 요청 보내기");
        player.sendMessage(ChatColor.YELLOW + "/tpaccept - TPA 요청 수락");
        player.sendMessage(ChatColor.YELLOW + "/tpdeny - TPA 요청 거부");
        player.sendMessage(ChatColor.YELLOW + "/tpcancel - TPA 요청 취소");
        player.sendMessage(ChatColor.YELLOW + "/tpastatus - TPA 상태 확인");
        player.sendMessage(ChatColor.GRAY + "");
        player.sendMessage(ChatColor.GRAY + "※ TPA는 같은 팀원들끼리만 사용할 수 있습니다!");
        player.sendMessage(ChatColor.GRAY + "※ 요청은 30초 후 자동으로 만료됩니다!");
        player.sendMessage(ChatColor.GRAY + "※ 요청 후 10초 쿨다운이 적용됩니다!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 첫 번째 인수: 온라인 플레이어 이름들
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}
