package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.commands.SubCommand;

import java.util.Collections;
import java.util.List;

/**
 * 게임 정보 명령어
 * 팀 설정법, 게임 방법, 커맨드 등을 안내합니다
 */
public class InfoCommand implements SubCommand {

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "========================================");
        sender.sendMessage(ChatColor.YELLOW + "          점령전 게임 가이드");
        sender.sendMessage(ChatColor.GOLD + "========================================");
        sender.sendMessage("");

        // 게임 설명
        sender.sendMessage(ChatColor.GREEN + "■ 게임 목표");
        sender.sendMessage(
                ChatColor.WHITE + "  점령지를 점령하여 " + ChatColor.YELLOW + "5점" + ChatColor.WHITE + "을 먼저 획득하는 팀이 승리!");
        sender.sendMessage(ChatColor.WHITE + "  • 기본 점령지(이그니스/아틀란스/크라이오시스/사이클론즈): " + ChatColor.AQUA + "1점");
        sender.sendMessage(ChatColor.WHITE + "  • 중앙 점령지(제네시스): " + ChatColor.GOLD + "2점");
        sender.sendMessage(ChatColor.WHITE + "  • 보너스 점수: " + ChatColor.LIGHT_PURPLE + "+1점");
        sender.sendMessage("");

        // 승리 조건
        sender.sendMessage(ChatColor.GREEN + "■ 승리 조건");
        sender.sendMessage(ChatColor.WHITE + "  방법 1: " + ChatColor.AQUA + "기본 4개 모두 점령" + ChatColor.WHITE + " + "
                + ChatColor.LIGHT_PURPLE + "보너스 1점" + ChatColor.WHITE + " = " + ChatColor.GOLD + "5점");
        sender.sendMessage(ChatColor.WHITE + "  방법 2: " + ChatColor.AQUA + "기본 2개" + ChatColor.WHITE + " + "
                + ChatColor.GOLD + "중앙 2점" + ChatColor.WHITE + " + " + ChatColor.LIGHT_PURPLE + "보너스 1점"
                + ChatColor.WHITE + " = " + ChatColor.GOLD + "5점");
        sender.sendMessage("");

        // 팀 설정
        sender.sendMessage(ChatColor.GREEN + "■ 팀 설정 방법");
        sender.sendMessage(ChatColor.WHITE + "  1. 마인크래프트 기본 명령어로 팀 생성:");
        sender.sendMessage(ChatColor.GRAY + "     /team add <팀이름>");
        sender.sendMessage(ChatColor.GRAY + "     /team modify <팀이름> color <색상>");
        sender.sendMessage(ChatColor.WHITE + "  2. 플레이어를 팀에 추가:");
        sender.sendMessage(ChatColor.GRAY + "     /team join <팀이름> <플레이어>");
        sender.sendMessage(ChatColor.WHITE + "  3. config.yml의 " + ChatColor.YELLOW + "participating_teams"
                + ChatColor.WHITE + "에 팀 이름 추가");
        sender.sendMessage(ChatColor.GRAY + "     예시: red, blue, green, yellow");
        sender.sendMessage("");

        // 점령 규칙
        sender.sendMessage(ChatColor.GREEN + "■ 점령 규칙");
        sender.sendMessage(ChatColor.WHITE + "  • 기본 점령지 (15×15 영역):");
        sender.sendMessage(ChatColor.GRAY + "    - 첫 점령: " + ChatColor.WHITE + "5분");
        sender.sendMessage(ChatColor.GRAY + "    - 재점령: " + ChatColor.WHITE + "10분");
        sender.sendMessage(ChatColor.WHITE + "  • 중앙 점령지 (15×15 영역):");
        sender.sendMessage(ChatColor.GRAY + "    - 첫 점령: " + ChatColor.WHITE + "10분");
        sender.sendMessage(ChatColor.GRAY + "    - 재점령: " + ChatColor.WHITE + "15분");
        sender.sendMessage(ChatColor.WHITE + "  • " + ChatColor.RED + "기본 점령지 3개 이상 점령 시 중앙 점령 불가!");
        sender.sendMessage(ChatColor.WHITE + "  • 여러 팀이 동시 진입 시 점령 시간 정지");
        sender.sendMessage("");

        // 점령지 효과
        sender.sendMessage(ChatColor.GREEN + "■ 점령지 효과 (점령한 팀만)");
        sender.sendMessage(ChatColor.RED + "  이그니스(불)" + ChatColor.WHITE + ": 화염 저항");
        sender.sendMessage(ChatColor.BLUE + "  아틀란스(물)" + ChatColor.WHITE + ": 수중 호흡, 돌고래의 가호");
        sender.sendMessage(ChatColor.AQUA + "  크라이오시스(얼음)" + ChatColor.WHITE + ": 구속 면역 (다른 팀은 구속)");
        sender.sendMessage(ChatColor.WHITE + "  사이클론즈(바람)" + ChatColor.WHITE + ": 신속");
        sender.sendMessage("");

        // 교환 시스템
        sender.sendMessage(ChatColor.GREEN + "■ 교환 시스템");
        sender.sendMessage(ChatColor.WHITE + "  " + ChatColor.YELLOW + "웅크리기(Shift)" + ChatColor.WHITE
                + " 상태에서 왼손에 아이템을 들고 " + ChatColor.YELLOW + "F키" + ChatColor.WHITE + "를 눌러 교환:");
        sender.sendMessage(ChatColor.GRAY + "  • " + ChatColor.AQUA + "청금석 64개" + ChatColor.GRAY + " → "
                + ChatColor.GREEN + "경험치 병 64개");
        sender.sendMessage(ChatColor.GRAY + "  • " + ChatColor.WHITE + "철괴 32개" + ChatColor.GRAY + " → "
                + ChatColor.GOLD + "빵 64개");
        sender.sendMessage("");

        // TPA 시스템
        sender.sendMessage(ChatColor.GREEN + "■ TPA 시스템 (팀원 전용)");
        sender.sendMessage(ChatColor.GRAY + "  • " + ChatColor.WHITE + "/tpa <플레이어>" + ChatColor.GRAY + " - 텔레포트 요청");
        sender.sendMessage(ChatColor.GRAY + "  • " + ChatColor.WHITE + "/tpaaccept" + ChatColor.GRAY + " - 요청 수락");
        sender.sendMessage(ChatColor.GRAY + "  • " + ChatColor.WHITE + "/tpadeny" + ChatColor.GRAY + " - 요청 거부");
        sender.sendMessage(ChatColor.GRAY + "    ※ 같은 팀원끼리만 가능, 30초 후 자동 만료");
        sender.sendMessage(ChatColor.GRAY + "    ※ 요청 쿨다운: " + ChatColor.YELLOW + "10초" + ChatColor.GRAY
                + ", 텔레포트 쿨다운: " + ChatColor.YELLOW + "5분");
        sender.sendMessage("");

        // 관리자 명령어
        sender.sendMessage(ChatColor.GREEN + "■ 관리자 명령어");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy start" + ChatColor.GRAY + " - 게임 시작");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy stop" + ChatColor.GRAY + " - 게임 종료");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy test start" + ChatColor.GRAY + " - 테스트 모드 (점령 시간 단축)");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy setpoint <지점>" + ChatColor.GRAY + " - 점령지 설정 (신호기 자동)");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy removepoint <지점>" + ChatColor.GRAY + " - 점령지 제거");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy teaminfo" + ChatColor.GRAY + " - 팀 정보 확인");
        sender.sendMessage(ChatColor.YELLOW + "  /occupy info" + ChatColor.GRAY + " - 이 도움말 표시");
        sender.sendMessage("");

        // 점령지 목록
        sender.sendMessage(ChatColor.GREEN + "■ 점령지 이름 (setpoint 사용 시)");
        sender.sendMessage(ChatColor.WHITE + "  center" + ChatColor.GRAY + " (제네시스), "
                + ChatColor.WHITE + "fire" + ChatColor.GRAY + " (이그니스), "
                + ChatColor.WHITE + "water" + ChatColor.GRAY + " (아틀란스)");
        sender.sendMessage(ChatColor.WHITE + "  ice" + ChatColor.GRAY + " (크라이오시스), "
                + ChatColor.WHITE + "wind" + ChatColor.GRAY + " (사이클론즈)");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "========================================");
        sender.sendMessage("");

        return true;
    }

    @Override
    public List<String> getTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
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
