package me.truec0der.mwhitelist;

import me.truec0der.mwhitelist.command.CommandController;
import me.truec0der.mwhitelist.command.CommandHandler;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.config.configs.MainConfig;
import me.truec0der.mwhitelist.impl.repository.RepositoryRegister;
import me.truec0der.mwhitelist.listener.PlayerJoinListener;
import me.truec0der.mwhitelist.misc.ThreadExecutor;
import me.truec0der.mwhitelist.model.enums.database.DatabaseType;
import me.truec0der.mwhitelist.service.ServiceRegister;
import me.truec0der.mwhitelist.service.database.DatabaseConnectionService;
import me.truec0der.mwhitelist.service.plugin.PluginUpdateServiceImpl;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class MWhitelist extends JavaPlugin {
    private ConfigRegister configRegister;
    private RepositoryRegister repositoryRegister;
    private ServiceRegister serviceRegister;
    private DatabaseConnectionService databaseConnectionService;
    private ThreadExecutor threadExecutor;

    @Override
    public void onEnable() {
        initConfig();
        initDatabaseConnection();
        initRepository();
        initService();
        initCommand();
        initListener();
        initMetrics();
        initPluginUpdateService();

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseConnectionService != null) databaseConnectionService.close();
        if (serviceRegister != null) serviceRegister.getWhitelistScheduleService().destroyExecutor();
        getLogger().info("Plugin disabled!");
    }

    private void initConfig() {
        configRegister = new ConfigRegister(this);
    }

    private void initDatabaseConnection() {
        databaseConnectionService = new DatabaseConnectionService(configRegister);
    }

    private void initRepository() {
        MainConfig mainConfig = configRegister.getMainConfig();
        DatabaseType databaseType = mainConfig.getDatabase().getType();

        repositoryRegister = new RepositoryRegister(this, databaseConnectionService, configRegister);
        repositoryRegister.init(databaseType);

        getLogger().info("Database " + databaseType.name() + " loaded!");
    }

    private void initService() {
        threadExecutor = new ThreadExecutor(this);
        serviceRegister = new ServiceRegister(repositoryRegister, configRegister, threadExecutor);
    }

    private void initCommand() {
        CommandController commandManager = new CommandController();

        CommandHandler commandHandler = new CommandHandler(commandManager, configRegister, serviceRegister);
        commandHandler.init();

        getCommand("mwhitelist").setExecutor(commandHandler);
        getCommand("mwhitelist").setTabCompleter(commandHandler);
    }

    private void initListener() {
        PlayerJoinListener playerJoinListener = new PlayerJoinListener(serviceRegister);
        getServer().getPluginManager().registerEvents(playerJoinListener, this);
    }

    private void initMetrics() {
        int pluginId = 20857;
        Metrics metrics = new Metrics(this, pluginId);
    }

    private void initPluginUpdateService() {
        MainConfig mainConfig = configRegister.getMainConfig();

        String destinationPath = Paths.get(getDataFolder().getAbsolutePath()).getParent().toString();
        String[] destinationPathSplit = getFile().getPath().split("/");
        String destinationName = destinationPathSplit[destinationPathSplit.length - 1];

        PluginUpdateServiceImpl pluginUpdateService = new PluginUpdateServiceImpl("https://truec0der.github.io/plugin/mWL.json", destinationPath, destinationName, configRegister.getLangConfig());
        if (mainConfig.getMain().getUpdate().isCheck())
            pluginUpdateService.handleCheck(getDescription().getVersion(), mainConfig.getMain().getUpdate().isAuto());
    }
}
