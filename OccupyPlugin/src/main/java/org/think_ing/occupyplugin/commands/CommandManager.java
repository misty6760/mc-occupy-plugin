package org.think_ing.occupyplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.think_ing.occupyplugin.OccupyPlugin;
import org.think_ing.occupyplugin.commands.subcommands.*;
import org.think_ing.occupyplugin.game.GameManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 명령어 라우터
 * 각 서브 명령어를 적절한 핸들러로 라우팅합니다
 */
public class CommandManager implements CommandExecutor {

    private final Map<String, SubCommand> subCommands;

    public CommandManager(OccupyPlugin plugin, GameManager gameManager) {
        this.subCommands = new HashMap<>();

        // 서브 명령어 등록
        registerSubCommand(new StartCommand(gameManager));
        registerSubCommand(new StopCommand(gameManager));
        registerSubCommand(new SetPointCommand(plugin, gameManager));
        registerSubCommand(new RemovePointCommand(plugin, gameManager));
        registerSubCommand(new TeamInfoCommand(plugin, gameManager));
        registerSubCommand(new InfoCommand());
        registerSubCommand(new TestCommand(gameManager));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsageMessage(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다.");
            sendUsageMessage(sender);
            return true;
        }

        return subCommand.execute(sender, args);
    }

    private void sendUsageMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "=== Occupy Plugin 명령어 ===");
        for (SubCommand subCommand : subCommands.values()) {
            sender.sendMessage(
                    ChatColor.WHITE + subCommand.getUsage() + ChatColor.GRAY + " - " + subCommand.getDescription());
        }
    }

    /**
     * 등록된 서브 명령어 목록 반환
     * 
     * @return 서브 명령어 맵
     */
    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }
}
