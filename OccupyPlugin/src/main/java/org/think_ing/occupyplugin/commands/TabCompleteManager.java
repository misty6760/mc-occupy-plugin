package org.think_ing.occupyplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 명령어 자동완성 관리
 */
public class TabCompleteManager implements TabCompleter {

    private final CommandManager commandManager;

    public TabCompleteManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                      @NotNull String alias, @NotNull String[] args) {
        // 첫 번째 인자: 서브 명령어 목록
        if (args.length == 1) {
            return commandManager.getSubCommands().keySet().stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 두 번째 이후 인자: 각 서브 명령어의 자동완성 위임
        if (args.length >= 2) {
            String subCommandName = args[0].toLowerCase();
            SubCommand subCommand = commandManager.getSubCommands().get(subCommandName);

            if (subCommand != null) {
                return subCommand.getTabComplete(sender, args);
            }
        }

        return new ArrayList<>();
    }
}
