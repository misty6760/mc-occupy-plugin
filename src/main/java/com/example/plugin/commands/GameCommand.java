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
import java.util.Collections;
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
                if (args.length > 1 && args[1].equalsIgnoreCase("test")) {
                    startGameTest(player);
                } else {
                    startGame(player);
                }
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
        player.sendMessage(ChatColor.YELLOW + "  /game start test" + ChatColor.WHITE + " - 테스트 모드 게임 시작 (한 명도 가능)");
        player.sendMessage(ChatColor.YELLOW + "  /game stop" + ChatColor.WHITE + " - 게임 중단");
        player.sendMessage(ChatColor.YELLOW + "  /game status" + ChatColor.WHITE + " - 게임 상태 및 점수 확인");
        player.sendMessage(ChatColor.YELLOW + "  /game reset" + ChatColor.WHITE + " - 게임 초기화");
        player.sendMessage(ChatColor.YELLOW + "  /game map" + ChatColor.WHITE + " - 테스트 맵 및 점령지 위치 표시");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "팀 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /join <팀명>" + ChatColor.WHITE + " - 팀에 가입");
        player.sendMessage(ChatColor.YELLOW + "  /leave" + ChatColor.WHITE + " - 팀에서 탈퇴");
        player.sendMessage(ChatColor.YELLOW + "  /team" + ChatColor.WHITE + " - 팀 정보 확인");
        player.sendMessage(ChatColor.YELLOW + "  /team rename <기존이름> <새이름>" + ChatColor.WHITE + " - 팀 이름 변경 (관리자)");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "점령 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /capture" + ChatColor.WHITE + " - 현재 위치 점령지 정보");
        player.sendMessage(ChatColor.YELLOW + "  /capture <점령지>" + ChatColor.WHITE + " - 특정 점령지 정보");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "정보 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /info" + ChatColor.WHITE + " - 전체 게임 정보");
        player.sendMessage(ChatColor.YELLOW + "  /info team" + ChatColor.WHITE + " - 팀 정보");
        player.sendMessage(ChatColor.YELLOW + "  /info capture" + ChatColor.WHITE + " - 점령지 정보");
        player.sendMessage(ChatColor.YELLOW + "  /info game" + ChatColor.WHITE + " - 게임 규칙");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "TPA 관련 (팀원 전용):");
        player.sendMessage(ChatColor.YELLOW + "  /tpa <플레이어>" + ChatColor.WHITE + " - 팀원에게 TPA 요청");
        player.sendMessage(ChatColor.YELLOW + "  /tpaccept" + ChatColor.WHITE + " - TPA 요청 수락");
        player.sendMessage(ChatColor.YELLOW + "  /tpdeny" + ChatColor.WHITE + " - TPA 요청 거절");
        player.sendMessage(ChatColor.YELLOW + "  /tpcancel" + ChatColor.WHITE + " - TPA 요청 취소");
        player.sendMessage(ChatColor.YELLOW + "  /tpastatus" + ChatColor.WHITE + " - TPA 상태 확인");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "교환 관련:");
        player.sendMessage(ChatColor.YELLOW + "  /exchange" + ChatColor.WHITE + " - 교환 가능한 아이템 목록");
        player.sendMessage(ChatColor.YELLOW + "  /exchange info" + ChatColor.WHITE + " - 내가 교환할 수 있는 아이템 확인");
        player.sendMessage(ChatColor.GRAY + "  왼손에 아이템을 들고 우클릭하여 교환");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "점령지 설정 (관리자):");
        player.sendMessage(ChatColor.YELLOW + "  /zone set <점령지이름>" + ChatColor.WHITE + " - 현재 위치에 점령지 설정");
        player.sendMessage(ChatColor.YELLOW + "  /zone list" + ChatColor.WHITE + " - 점령지 목록 보기");
        player.sendMessage(ChatColor.YELLOW + "  /zone reload" + ChatColor.WHITE + " - 설정 다시 로드");
        player.sendMessage(ChatColor.YELLOW + "  /zone help" + ChatColor.WHITE + " - 도움말 보기");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.AQUA + "테스트 명령어 (관리자):");
        player.sendMessage(ChatColor.YELLOW + "  /test capture-time <시간>" + ChatColor.WHITE + " - 점령 시간 설정 (초)");
        player.sendMessage(ChatColor.YELLOW + "  /test capture-time reset" + ChatColor.WHITE + " - 점령 시간 원래대로 복구");
        player.sendMessage(ChatColor.YELLOW + "  /test capture-time status" + ChatColor.WHITE + " - 현재 점령 시간 확인");
        player.sendMessage(ChatColor.YELLOW + "  /test help" + ChatColor.WHITE + " - 테스트 명령어 도움말");
        player.sendMessage("");
        
        player.sendMessage(ChatColor.GRAY + "팁: Tab 키를 눌러 명령어를 자동 완성할 수 있습니다!");
        player.sendMessage(ChatColor.GRAY + "팁: /game map으로 점령지 위치를 확인하세요!");
        player.sendMessage(ChatColor.GRAY + "팁: /info game으로 게임 규칙을 확인하세요!");
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
     * 테스트 모드 게임 시작 (한 명이어도 가능)
     * @param player 명령어 실행자
     */
    private void startGameTest(Player player) {
        if (captureManager.isGameActive()) {
            player.sendMessage(ChatColor.RED + "게임이 이미 진행 중입니다!");
            return;
        }

        // 테스트 모드에서는 최소 인원 제한 없음
        captureManager.startGame();
        player.sendMessage(ChatColor.GREEN + "땅따먹기 게임이 테스트 모드로 시작되었습니다!");
        player.sendMessage(ChatColor.YELLOW + "테스트 모드: 최소 인원 제한이 없습니다.");
    }

    /**
     * 게임 중단
     * @param player 명령어 실행자
     */
    private void stopGame(Player player) {
        if (!captureManager.isGameActive()) {
            // 테스트 모드인지 확인
            if (captureManager.isTestMode()) {
                player.sendMessage(ChatColor.YELLOW + "테스트 모드가 활성화되어 있습니다. 테스트 모드를 비활성화합니다.");
                captureManager.resetTestCaptureTime();
                player.sendMessage(ChatColor.GREEN + "테스트 모드가 비활성화되었습니다.");
            } else {
                player.sendMessage(ChatColor.RED + "게임이 진행 중이 아닙니다!");
            }
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
        if (args.length == 1) {
            // 첫 번째 인수: 게임 명령어
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            if ("start".startsWith(input)) {
                completions.add("start");
            }
            if ("stop".startsWith(input)) {
                completions.add("stop");
            }
            if ("status".startsWith(input)) {
                completions.add("status");
            }
            if ("reset".startsWith(input)) {
                completions.add("reset");
            }
            if ("map".startsWith(input)) {
                completions.add("map");
            }
            if ("help".startsWith(input)) {
                completions.add("help");
            }
            
            return completions;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            // /game start의 두 번째 인수
            String input = args[1].toLowerCase();
            if ("test".startsWith(input)) {
                return Collections.singletonList("test");
            }
        }
        
        return Collections.emptyList();
    }
}
