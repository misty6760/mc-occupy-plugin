package org.think_ing.occupyplugin.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.commands.SubCommand;
import org.think_ing.occupyplugin.game.GameManager;

/**
 * 게임 종료 명령어
 */
public class StopCommand implements SubCommand {
    
    private final GameManager gameManager;
    
    public StopCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!gameManager.isGameRunning()) {
            sender.sendMessage(ChatColor.RED + "게임이 시작되지 않았습니다.");
            return true;
        }
        
        gameManager.stopGame();
        sender.sendMessage(ChatColor.YELLOW + "점령전이 중지되었습니다.");
        return true;
    }
    
    @Override
    public String getName() {
        return "stop";
    }
    
    @Override
    public String getDescription() {
        return "점령전을 중지합니다";
    }
    
    @Override
    public String getUsage() {
        return "/occupy stop";
    }
}

