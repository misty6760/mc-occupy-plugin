package com.example.plugin.commands;

import com.example.plugin.capture.CaptureManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 테스트 전용 명령어 처리
 * /test - 테스트 명령어 도움말
 * /test capture-time <시간> - 점령 시간 설정 (초)
 * /test capture-time reset - 점령 시간 원래대로 복구
 * /test capture-time status - 현재 점령 시간 확인
 */
public class TestCommand implements CommandExecutor, TabCompleter {
    private final CaptureManager captureManager;
    
    // 원래 점령 시간 저장 (복구용)
    private int originalCaptureTime = 300; // 5분 (300초)
    private boolean isTestMode = false;

    public TestCommand(CaptureManager captureManager) {
        this.captureManager = captureManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("landcapture.admin")) {
            player.sendMessage(ChatColor.RED + "이 명령어는 관리자만 사용할 수 있습니다!");
            return true;
        }

        if (args.length == 0) {
            showTestHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "capture-time":
                handleCaptureTime(player, args);
                break;
            case "help":
                showTestHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다. /test help를 사용하세요.");
                break;
        }

        return true;
    }

    /**
     * 점령 시간 관련 명령어 처리
     */
    private void handleCaptureTime(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "사용법: /test capture-time <시간|reset|status>");
            player.sendMessage(ChatColor.YELLOW + "예시: /test capture-time 10 (10초로 설정)");
            player.sendMessage(ChatColor.YELLOW + "예시: /test capture-time reset (원래대로 복구)");
            player.sendMessage(ChatColor.YELLOW + "예시: /test capture-time status (현재 시간 확인)");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "reset":
                resetCaptureTime(player);
                break;
            case "status":
                showCaptureTimeStatus(player);
                break;
            default:
                try {
                    int time = Integer.parseInt(action);
                    setCaptureTime(player, time);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "올바른 숫자를 입력하세요!");
                    player.sendMessage(ChatColor.YELLOW + "사용법: /test capture-time <시간|reset|status>");
                }
                break;
        }
    }

    /**
     * 점령 시간 설정
     */
    private void setCaptureTime(Player player, int time) {
        if (time < 1 || time > 3600) {
            player.sendMessage(ChatColor.RED + "점령 시간은 1초에서 3600초(1시간) 사이여야 합니다!");
            return;
        }

        // CaptureManager의 점령 시간 설정
        captureManager.setTestCaptureTime(time);
        isTestMode = true;

        player.sendMessage(ChatColor.GREEN + "점령 시간을 " + time + "초로 설정했습니다!");
        player.sendMessage(ChatColor.YELLOW + "테스트 모드가 활성화되었습니다.");
        player.sendMessage(ChatColor.GRAY + "원래대로 복구하려면: /test capture-time reset");
    }

    /**
     * 점령 시간 원래대로 복구
     */
    private void resetCaptureTime(Player player) {
        captureManager.setTestCaptureTime(originalCaptureTime);
        isTestMode = false;

        player.sendMessage(ChatColor.GREEN + "점령 시간을 원래대로 복구했습니다! (" + originalCaptureTime + "초)");
        player.sendMessage(ChatColor.YELLOW + "테스트 모드가 비활성화되었습니다.");
    }

    /**
     * 현재 점령 시간 상태 확인
     */
    private void showCaptureTimeStatus(Player player) {
        int currentTime = captureManager.getTestCaptureTime();
        
        player.sendMessage(ChatColor.GOLD + "=== 점령 시간 상태 ===");
        player.sendMessage(ChatColor.YELLOW + "현재 점령 시간: " + ChatColor.WHITE + currentTime + "초");
        player.sendMessage(ChatColor.YELLOW + "원래 점령 시간: " + ChatColor.WHITE + originalCaptureTime + "초");
        
        if (isTestMode) {
            player.sendMessage(ChatColor.GREEN + "상태: " + ChatColor.WHITE + "테스트 모드 활성화");
        } else {
            player.sendMessage(ChatColor.GRAY + "상태: " + ChatColor.WHITE + "일반 모드");
        }
        
        player.sendMessage(ChatColor.GRAY + "복구 명령어: /test capture-time reset");
    }

    /**
     * 테스트 명령어 도움말 표시
     */
    private void showTestHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 테스트 명령어 도움말 ===");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "점령 시간 테스트:");
        player.sendMessage(ChatColor.YELLOW + "  /test capture-time <시간>" + ChatColor.WHITE + " - 점령 시간 설정 (초)");
        player.sendMessage(ChatColor.YELLOW + "  /test capture-time reset" + ChatColor.WHITE + " - 점령 시간 원래대로 복구");
        player.sendMessage(ChatColor.YELLOW + "  /test capture-time status" + ChatColor.WHITE + " - 현재 점령 시간 확인");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.GRAY + "예시:");
        player.sendMessage(ChatColor.GRAY + "  /test capture-time 10 - 점령 시간을 10초로 설정");
        player.sendMessage(ChatColor.GRAY + "  /test capture-time reset - 원래 시간(5분)으로 복구");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.RED + "주의: 이 명령어는 테스트용입니다!");
        player.sendMessage(ChatColor.RED + "실제 게임에서는 원래 시간으로 복구하세요.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 첫 번째 인수: 테스트 명령어 (메모리 효율적인 방식)
            String input = args[0].toLowerCase();
            if ("capture-time".startsWith(input)) {
                return Collections.singletonList("capture-time");
            }
            if ("help".startsWith(input)) {
                return Collections.singletonList("help");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("capture-time")) {
            // 두 번째 인수: capture-time 옵션
            String input = args[1].toLowerCase();
            if ("reset".startsWith(input)) {
                return Collections.singletonList("reset");
            }
            if ("status".startsWith(input)) {
                return Collections.singletonList("status");
            }
        }
        
        return Collections.emptyList();
    }
}
