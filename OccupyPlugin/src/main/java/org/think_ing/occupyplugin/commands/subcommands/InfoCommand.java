package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.commands.SubCommand;

/**
 * 게임 정보 명령어
 * 팀 설정법, 게임 방법, 커맨드 등을 안내합니다
 */
public class InfoCommand implements SubCommand {
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "========================================");
        sender.sendMessage(ChatColor.YELLOW + "          땅따먹기 게임 가이드");
        sender.sendMessage(ChatColor.GOLD + "========================================");
        sender.sendMessage("");
        
        // 게임 설명
        sender.sendMessage(ChatColor.GREEN + "■ 게임 목표");
        sender.sendMessage(ChatColor.WHITE + "  • 점령지를 점령하여 " + ChatColor.YELLOW + "5점" + ChatColor.WHITE + "을 먼저 획득하는 팀이 승리!");
        sender.sendMessage(ChatColor.WHITE + "  • 기본 점령지(물/불/얼음/바람): " + ChatColor.AQUA + "1점");
        sender.sendMessage(ChatColor.WHITE + "  • 중앙 점령지: " + ChatColor.GOLD + "2점");
        sender.sendMessage("");
        
        // 팀 설정
        sender.sendMessage(ChatColor.GREEN + "■ 팀 설정 방법");
        sender.sendMessage(ChatColor.WHITE + "  1. 마인크래프트 기본 명령어로 팀 생성:");
        sender.sendMessage(ChatColor.GRAY + "     /team add <팀이름>");
        sender.sendMessage(ChatColor.WHITE + "  2. 플레이어를 팀에 추가:");
        sender.sendMessage(ChatColor.GRAY + "     /team join <팀이름> <플레이어>");
        sender.sendMessage(ChatColor.WHITE + "  3. config.yml의 " + ChatColor.YELLOW + "participating_teams" + ChatColor.WHITE + "에 팀 이름 추가");
        sender.sendMessage(ChatColor.GRAY + "     예시: red, blue, green, yellow");
        sender.sendMessage("");
        
        // 점령 규칙
        sender.sendMessage(ChatColor.GREEN + "■ 점령 규칙");
        sender.sendMessage(ChatColor.WHITE + "  • 기본 점령지:");
        sender.sendMessage(ChatColor.GRAY + "    - 첫 점령: " + ChatColor.WHITE + "5분");
        sender.sendMessage(ChatColor.GRAY + "    - 재점령: " + ChatColor.WHITE + "10분");
        sender.sendMessage(ChatColor.WHITE + "  • 중앙 점령지:");
        sender.sendMessage(ChatColor.GRAY + "    - 첫 점령: " + ChatColor.WHITE + "10분");
        sender.sendMessage(ChatColor.GRAY + "    - 재점령: " + ChatColor.WHITE + "15분");
        sender.sendMessage(ChatColor.WHITE + "  • " + ChatColor.RED + "기본 점령지 3개 이상 점령 시 중앙 점령 불가!");
        sender.sendMessage("");
        
        // 점령지 효과
        sender.sendMessage(ChatColor.GREEN + "■ 점령지 효과 (점령한 팀만)");
        sender.sendMessage(ChatColor.RED + "  불" + ChatColor.WHITE + ": 화염 저항");
        sender.sendMessage(ChatColor.BLUE + "  물" + ChatColor.WHITE + ": 수중 호흡, 돌고래의 가호");
        sender.sendMessage(ChatColor.AQUA + "  얼음" + ChatColor.WHITE + ": 구속 면역 (다른 팀은 구속됨)");
        sender.sendMessage(ChatColor.WHITE + "  바람" + ChatColor.WHITE + ": 신속");
        sender.sendMessage("");
        
        // 기타 기능
        sender.sendMessage(ChatColor.GREEN + "■ 기타 기능");
        sender.sendMessage(ChatColor.WHITE + "  • 리스폰: Y=100에서 느린 낙하와 함께 스폰");
        sender.sendMessage(ChatColor.WHITE + "  • TPA: 같은 팀원에게만 텔레포트 요청 가능");
        sender.sendMessage(ChatColor.GRAY + "    - " + ChatColor.WHITE + "/tpa <플레이어>" + ChatColor.GRAY + " : 텔레포트 요청");
        sender.sendMessage(ChatColor.GRAY + "    - " + ChatColor.WHITE + "/tpaaccept" + ChatColor.GRAY + " : 요청 수락");
        sender.sendMessage(ChatColor.GRAY + "    - " + ChatColor.WHITE + "/tpadeny" + ChatColor.GRAY + " : 요청 거부");
        sender.sendMessage("");
        
        // 관리자 명령어
        sender.sendMessage(ChatColor.GREEN + "■ 관리자 명령어");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy start " + ChatColor.GRAY + "- 게임 시작");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy stop " + ChatColor.GRAY + "- 게임 종료");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy setpoint <지점> " + ChatColor.GRAY + "- 점령지 위치 설정 (신호기 자동 설치)");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy removepoint <지점> " + ChatColor.GRAY + "- 점령지 위치 제거");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy teaminfo " + ChatColor.GRAY + "- 팀 정보 확인");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy info " + ChatColor.GRAY + "- 이 도움말 표시");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "========================================");
        sender.sendMessage("");
        
        return true;
    }
    
    @Override
    public String getName() {
        return "info";
    }
    
    @Override
    public String getDescription() {
        return "게임 가이드 및 명령어 정보를 표시합니다";
    }
    
    @Override
    public String getUsage() {
        return "/occupy info";
    }
}

