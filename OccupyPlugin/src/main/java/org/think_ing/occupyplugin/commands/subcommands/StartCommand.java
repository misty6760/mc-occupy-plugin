package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.commands.SubCommand;
import org.think_ing.occupyplugin.game.GameManager;

/**
 * 게임 시작 명령어
 */
public class StartCommand implements SubCommand {
    
    private final GameManager gameManager;
    
    public StartCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (gameManager.isGameRunning()) {
            sender.sendMessage(ChatColor.RED + "이미 게임이 진행 중입니다. 종료하려면 /occupy stop을 입력하세요.");
            return true;
        }
        
        gameManager.startGame();
        sender.sendMessage(ChatColor.GREEN + "점령전이 시작되었습니다.");
        return true;
    }
    
    @Override
    public String getName() {
        return "start";
    }
    
    @Override
    public String getDescription() {
        return "점령전을 시작합니다";
    }
    
    @Override
    public String getUsage() {
        return "/occupy start";
    }
}

