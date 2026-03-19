package com.comonier.Trade;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class IntegrationManager {
    private Economy econ = null;
    private Chat chat = null;
    private final Trade plugin;

    public IntegrationManager(Trade plugin) {
        this.plugin = plugin;
        setupEconomy();
        setupChat();
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return false;
        chat = rsp.getProvider();
        return chat != null;
    }

    // MÉTODO QUE O MAVEN PEDIU (Puxa cor do grupo/prefixo)
    public String getPlayerNameWithPrefix(Player player) {
        if (chat == null) return "§e" + player.getName();
        String prefix = chat.getPlayerPrefix(player);
        if (prefix == null || prefix.isEmpty()) return "§e" + player.getName();
        return ChatColor.translateAlternateColorCodes('&', prefix + player.getName());
    }

    public boolean hasEconomy() {
        return econ != null;
    }

    public double getBalance(Player player) {
        if (hasEconomy() == false) return 0.0;
        return econ.getBalance(player);
    }

    public void withdraw(Player player, double amount) {
        if (hasEconomy() == false) return;
        BigDecimal balance = BigDecimal.valueOf(getBalance(player));
        BigDecimal toWithdraw = BigDecimal.valueOf(amount);
        if (balance.compareTo(toWithdraw) >= 0) {
            econ.withdrawPlayer(player, toWithdraw.setScale(2, RoundingMode.HALF_UP).doubleValue());
        }
    }

    public void deposit(Player player, double amount) {
        if (hasEconomy() == false) return;
        BigDecimal toDeposit = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        econ.depositPlayer(player, toDeposit.doubleValue());
    }

    // Métodos do GriefPrevention
    private PlayerData getGPData(Player p) {
        if (Bukkit.getPluginManager().getPlugin("GriefPrevention") == null) return null;
        return GriefPrevention.instance.dataStore.getPlayerData(p.getUniqueId());
    }

    public int getAccruedBlocks(Player p) {
        PlayerData data = getGPData(p);
        return (data != null) ? data.getAccruedClaimBlocks() : 0;
    }

    public int getBonusBlocks(Player p) {
        PlayerData data = getGPData(p);
        return (data != null) ? data.getBonusClaimBlocks() : 0;
    }

    public int getRemainingBlocks(Player p) {
        PlayerData data = getGPData(p);
        return (data != null) ? data.getRemainingClaimBlocks() : 0;
    }

    public void addClaimBlocks(Player player, int amount) {
        PlayerData data = getGPData(player);
        if (data != null) data.setAccruedClaimBlocks(data.getAccruedClaimBlocks() + amount);
    }

    public void removeClaimBlocks(Player player, int amount) {
        PlayerData data = getGPData(player);
        if (data != null && data.getRemainingClaimBlocks() >= amount) {
            data.setAccruedClaimBlocks(data.getAccruedClaimBlocks() - amount);
        }
    }
}
