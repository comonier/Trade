package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class Trade extends JavaPlugin {
    private static Trade instance;
    private MessageManager msgManager;
    private IntegrationManager integration;
    private TradeManager tradeManager;
    private LogManager logManager;
    private TradeFinalizer tradeFinalizer;
    private TradeChestManager tradeChestManager;
    private SafetyHandler safetyHandler;
    private TradeSyncManager tradeSyncManager; // Novo Manager de Sincronismo

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        createMessagesFiles();
        createLogsFolder();

        this.msgManager = new MessageManager(this);
        this.integration = new IntegrationManager(this);
        this.tradeManager = new TradeManager(this);
        this.logManager = new LogManager(this);
        this.tradeFinalizer = new TradeFinalizer(this);
        this.tradeChestManager = new TradeChestManager(this);
        this.safetyHandler = new SafetyHandler(this);
        this.tradeSyncManager = new TradeSyncManager(this); // Inicialização

        try {
            if (getCommand("trade") != null) {
                getCommand("trade").setExecutor(new TradeCommand(this));
                getCommand("trade").setTabCompleter(new TradeTabCompleter());
            }
            
            getServer().getPluginManager().registerEvents(new TradeListener(this), this);
            
            String v = getDescription().getVersion();
            getLogger().info("Trade v" + v + " enabled with Sync System.");
        } catch (Exception e) {
            getLogger().severe("Error: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (safetyHandler != null) {
            safetyHandler.restoreAllActiveTrades();
        }
        instance = null;
    }

    private void createMessagesFiles() {
        String[] files = {"messages_pt.yml", "messages_en.yml"};
        for (String f : files) {
            File file = new File(getDataFolder(), f);
            if (file.exists() == false) {
                saveResource(f, false);
            }
        }
    }

    private void createLogsFolder() {
        File folder = new File(getDataFolder(), "logs");
        if (folder.exists() == false) {
            folder.mkdirs();
        }
    }

    public static Trade getInstance() { return instance; }
    public MessageManager getMsgManager() { return msgManager; }
    public IntegrationManager getIntegration() { return integration; }
    public TradeManager getTradeManager() { return tradeManager; }
    public LogManager getLogManager() { return logManager; }
    public TradeFinalizer getTradeFinalizer() { return tradeFinalizer; }
    public TradeChestManager getTradeChestManager() { return tradeChestManager; }
    public TradeSyncManager getTradeSyncManager() { return tradeSyncManager; } // Getter para o Listener
}
