package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.commands.SubCommand;
import org.think_ing.occupyplugin.game.GameManager;

import java.util.Collections;
import java.util.List;

/**
 * 테스트 모드 명령어
 * 점령 시간을 단축하여 빠른 테스트를 가능하게 합니다
 */
public class TestCommand implements SubCommand {

    private final GameManager gameManager;

    public TestCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + getUsage());
            return true;
        }

        String subCommand = args[1].toLowerCase();

        if (subCommand.equals("start")) {
            if (gameManager.isGameRunning()) {
                sender.sendMessage(ChatColor.RED + "이미 게임이 진행 중입니다. 종료하려면 /occupy stop을 입력하세요.");
                return true;
            }

            gameManager.startTestGame();
            sender.sendMessage(ChatColor.GREEN + "테스트 모드로 점령전이 시작되었습니다.");
            sender.sendMessage(ChatColor.GRAY + "• 기본 점령지 점령 시간: " + ChatColor.YELLOW + "1분");
            sender.sendMessage(ChatColor.GRAY + "• 중앙 점령지 점령 시간: " + ChatColor.YELLOW + "2분");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "알 수 없는 서브 명령어입니다.");
            sender.sendMessage(ChatColor.YELLOW + getUsage());
            return true;
        }
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "테스트 모드로 게임을 시작합니다 (점령 시간 단축)";
    }

    @Override
    public String getUsage() {
        return "/occupy test <start>";
    }

    @Override
    public List<String> getTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return Collections.singletonList("start");
        }
        return Collections.emptyList();
    }
}
