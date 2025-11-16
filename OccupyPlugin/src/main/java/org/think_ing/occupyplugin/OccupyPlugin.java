package org.think_ing.occupyplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.think_ing.occupyplugin.commands.CommandManager;
import org.think_ing.occupyplugin.commands.TabCompleteManager;
import org.think_ing.occupyplugin.config.TeamConfigManager;
import org.think_ing.occupyplugin.events.ExchangeListener;
import org.think_ing.occupyplugin.events.TeamListener;
import org.think_ing.occupyplugin.game.GameManager;
import org.think_ing.occupyplugin.tpa.TPACommandExecutor;
import org.think_ing.occupyplugin.tpa.TPAManager;
import org.think_ing.occupyplugin.tpa.TeamValidator;

import java.util.logging.Logger;

public final class OccupyPlugin extends JavaPlugin {

    private GameManager gameManager;
    private TPAManager tpaManager;
    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        gameManager = new GameManager(this);
        gameManager.loadConfig();

        // 명령어 등록
        PluginCommand occupyCommand = getCommand("occupy");
        if (occupyCommand != null) {
            CommandManager commandManager = new CommandManager(this, gameManager);
            occupyCommand.setExecutor(commandManager);
            occupyCommand.setTabCompleter(new TabCompleteManager(commandManager));
        } else {
            logger.severe("Could not find the 'occupy' command! Make sure it is registered in plugin.yml.");
        }

        // TPA 명령어 등록
        tpaManager = new TPAManager(this);
        TeamValidator teamValidator = new TeamValidator();
        TPACommandExecutor tpaExecutor = new TPACommandExecutor(tpaManager, teamValidator);
        
        PluginCommand tpaCommand = getCommand("tpa");
        PluginCommand tpaAcceptCommand = getCommand("tpaaccept");
        PluginCommand tpaDenyCommand = getCommand("tpadeny");
        
        if (tpaCommand != null) tpaCommand.setExecutor(tpaExecutor);
        if (tpaAcceptCommand != null) tpaAcceptCommand.setExecutor(tpaExecutor);
        if (tpaDenyCommand != null) tpaDenyCommand.setExecutor(tpaExecutor);

        // 이벤트 리스너 등록
        TeamConfigManager teamConfigManager = new TeamConfigManager(this);
        getServer().getPluginManager().registerEvents(new TeamListener(teamConfigManager), this);
        getServer().getPluginManager().registerEvents(new ExchangeListener(this), this);

        logger.info(ChatColor.DARK_GREEN + "========================================");
        logger.info(ChatColor.GREEN + "      OccupyPlugin has been enabled!    ");
        logger.info(ChatColor.DARK_GREEN + "========================================");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (gameManager != null) {
            gameManager.stopGame();
        }
        if (tpaManager != null) {
            tpaManager.clearAllRequests();
        }
        logger.info(ChatColor.DARK_RED + "========================================");
        logger.info(ChatColor.RED + "      OccupyPlugin has been disabled!   ");
        logger.info(ChatColor.DARK_RED + "========================================");
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
