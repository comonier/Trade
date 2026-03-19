package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TradeSyncManager {
    private final Trade plugin;

    public TradeSyncManager(Trade plugin) {
        this.plugin = plugin;
    }

    public void sync(TradeSession s, Inventory top) {
        // Redesenha os botões, lãs e status no objeto da Inventory
        TradeInventory.updateButtons(top, s);

        // Força o pacote de atualização para quem estiver olhando
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getOpenInventory().getTopInventory().equals(top)) {
                viewer.updateInventory();
            }
        }
    }
}
