package com.example.plugin.commands;

import com.example.plugin.capture.CaptureManager;
import com.example.plugin.capture.CaptureZone;
import com.example.plugin.team.Team;
import com.example.plugin.team.TeamManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 게임 관련 명령어 처리
 * /game start - 게임 시작
 * /game stop - 게임 중단
 * /game status - 게임 상태 확인
 */
public class GameCommand implements CommandExecutor, TabCompleter {
    private final CaptureManager captureManager;
    private final TeamManager teamManager;

    public GameCommand(CaptureManager captureManager, TeamManager teamManager) {
        this.captureManager = captureManager;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showGameHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                startGame(player);
                break;
            case "stop":
                stopGame(player);
                break;
            case "status":
                showGameStatus(player);
                break;
            case "reset":
                resetGame(player);
                break;
            case "map":
                showTestMap(player);
                break;
            case "help":
                showGameHelp(player);
                break;
            default:
                showGameHelp(player);
                break;
        }

        return true;
    }

    /**
     * 게임 도움말 표시
     * @param player 대상 플레이어
     */
    private void showGameHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 땅따먹기 게임 명령어 ===");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "기본 명령어:");
        player.sendMessage(ChatColor.YELLOW + "  /game start" + ChatColor.WHITE + " - 게임 시작 (최소 6명 필요)");
        player.sendMessage(ChatColor.YELLOW + "  /game stop" + ChatColor.WHITE + " - 게임 중단");
        player.sendMessage(ChatColor.YELLOW + "  /game status" + ChatColor.WHITE + " - 게임 상태 및 점수 확인");
        player.sendMessage(ChatColor.YELLOW + "  /game reset" + ChatColor.WHITE + " - 게임 초기화");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "맵 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /game map" + ChatColor.WHITE + " - 테스트 맵 및 점령지 위치 표시");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "팀 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /join <팀명>" + ChatColor.WHITE + " - 팀에 가입");
        player.sendMessage(ChatColor.YELLOW + "  /leave" + ChatColor.WHITE + " - 팀에서 탈퇴");
        player.sendMessage(ChatColor.YELLOW + "  /team" + ChatColor.WHITE + " - 팀 정보 확인");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "점령 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /capture <점령지>" + ChatColor.WHITE + " - 점령지 점령 시작");
        player.sendMessage(ChatColor.YELLOW + "  /capture list" + ChatColor.WHITE + " - 점령지 목록 확인");
        player.sendMessage(ChatColor.YELLOW + "  /capture stop" + ChatColor.WHITE + " - 점령 중단");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "TPA 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /tpa <플레이어>" + ChatColor.WHITE + " - 팀원에게 TPA 요청");
        player.sendMessage(ChatColor.YELLOW + "  /tpaccept" + ChatColor.WHITE + " - TPA 요청 수락");
        player.sendMessage(ChatColor.YELLOW + "  /tpdeny" + ChatColor.WHITE + " - TPA 요청 거절");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "교환 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /exchange" + ChatColor.WHITE + " - 교환 가능한 아이템 확인");
        player.sendMessage(ChatColor.YELLOW + "  /exchange <아이템> <수량>" + ChatColor.WHITE + " - 아이템 교환");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "신호기 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /beacon create" + ChatColor.WHITE + " - 신호기 구조 생성");
        player.sendMessage(ChatColor.YELLOW + "  /beacon reset" + ChatColor.WHITE + " - 신호기 색상 초기화");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.GRAY + "팁: Tab 키를 눌러 명령어를 자동 완성할 수 있습니다!");
        player.sendMessage(ChatColor.GRAY + "팁: /game map으로 점령지 위치를 확인하세요!");
    }

    /**
     * 게임 시작
     * @param player 명령어 실행자
     */
    private void startGame(Player player) {
        if (captureManager.isGameActive()) {
            player.sendMessage(ChatColor.RED + "게임이 이미 진행 중입니다!");
            return;
        }

        // 팀원 수 확인
        int totalPlayers = 0;
        for (Team team : teamManager.getAllTeams()) {
            totalPlayers += team.getMemberCount();
        }

        if (totalPlayers < 6) { // 최소 2팀 * 3명
            player.sendMessage(ChatColor.RED + "게임을 시작하려면 최소 6명의 플레이어가 필요합니다!");
            player.sendMessage(ChatColor.YELLOW + "현재 팀원 수: " + totalPlayers + "명");
            return;
        }

        captureManager.startGame();
        player.sendMessage(ChatColor.GREEN + "땅따먹기 게임이 시작되었습니다!");
    }

    /**
     * 게임 중단
     * @param player 명령어 실행자
     */
    private void stopGame(Player player) {
        if (!captureManager.isGameActive()) {
            player.sendMessage(ChatColor.RED + "게임이 진행 중이 아닙니다!");
            return;
        }

        captureManager.stopGame();
        player.sendMessage(ChatColor.RED + "게임이 중단되었습니다!");
    }

    /**
     * 게임 상태 표시
     * @param player 대상 플레이어
     */
    private void showGameStatus(Player player) {
        if (captureManager.isGameActive()) {
            player.sendMessage(ChatColor.GREEN + "게임 상태: 진행 중");
            
            // 팀 점수 표시
            player.sendMessage(ChatColor.GOLD + "=== 팀 점수 ===");
            for (Team team : teamManager.getAllTeams()) {
                player.sendMessage(team.toString());
            }
            
            // 점령지 상태 표시
            player.sendMessage(ChatColor.GOLD + "=== 점령지 상태 ===");
            for (CaptureZone zone : captureManager.getAllCaptureZones()) {
                player.sendMessage(zone.toString());
            }
        } else {
            player.sendMessage(ChatColor.RED + "게임 상태: 대기 중");
        }
    }

    /**
     * 게임 초기화
     * @param player 명령어 실행자
     */
    private void resetGame(Player player) {
        if (captureManager.isGameActive()) {
            captureManager.stopGame();
        }
        
        teamManager.resetAllScores();
        player.sendMessage(ChatColor.GREEN + "게임이 초기화되었습니다!");
    }

    /**
     * 게임 맵 표시
     * @param player 대상 플레이어
     */
    @SuppressWarnings("deprecation")
    private void showTestMap(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 땅따먹기 게임 맵 ===");
        player.sendMessage(ChatColor.YELLOW + "점령지 위치 (클릭하여 이동):");
        player.sendMessage("");
        
        for (CaptureZone zone : captureManager.getAllCaptureZones()) {
            String status = zone.getCurrentTeam() != null ? 
                ChatColor.GREEN + "점령됨 (" + zone.getCurrentTeam() + ")" : 
                ChatColor.GRAY + "미점령";
            
            // 클릭 가능한 좌표 생성
            TextComponent coordsComponent = new TextComponent();
            coordsComponent.setText(ChatColor.YELLOW + "[" + 
                ChatColor.UNDERLINE + zone.getCenter().getBlockX() + ", " + zone.getCenter().getBlockZ() + 
                ChatColor.YELLOW + "]");
            
            // 클릭 이벤트 설정 (텔레포트 명령어 실행) - 사용자의 y좌표 사용
            String tpCommand = "/tp " + zone.getCenter().getBlockX() + " " + player.getLocation().getBlockY() + " " + zone.getCenter().getBlockZ();
            coordsComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand));
            
            // 호버 이벤트 설정 (도구 설명)
            String hoverText = ChatColor.GREEN + "클릭하여 " + zone.getType().getDisplayName() + " 점령지로 이동\n" +
                              ChatColor.GRAY + "좌표: " + zone.getCenter().getBlockX() + ", " + zone.getCenter().getBlockZ();
            coordsComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder(hoverText).create()));
            
            // 메시지 구성
            TextComponent message = new TextComponent();
            message.addExtra(ChatColor.AQUA + "• " + zone.getType().getDisplayName() + 
                ChatColor.WHITE + " - ");
            message.addExtra(coordsComponent);
            message.addExtra(" " + status);
            
            player.spigot().sendMessage(message);
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== 맵 레이아웃 (20x20 테스트) ===");
        player.sendMessage(ChatColor.WHITE + "    북동쪽(얼음)     북서쪽(물)");
        player.sendMessage(ChatColor.WHITE + "    (8,-8)        (-8,-8)");
        player.sendMessage(ChatColor.WHITE + "           중앙(중심)");
        player.sendMessage(ChatColor.WHITE + "           (0,0)");
        player.sendMessage(ChatColor.WHITE + "    남동쪽(불)     남서쪽(바람)");
        player.sendMessage(ChatColor.WHITE + "    (8,8)         (-8,8)");
        
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "팁: 좌표를 클릭하면 해당 점령지의 중심으로 이동합니다!");
        player.sendMessage(ChatColor.GRAY + "팁: /tp <x> <y> <z> 명령어로도 이동할 수 있습니다.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 첫 번째 인수: 게임 명령어
            List<String> gameCommands = Arrays.asList("start", "stop", "status", "reset", "map", "help");
            for (String cmd : gameCommands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        }
        
        return completions;
    }
}
