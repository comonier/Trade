package com.comonier.Trade;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TradeFinalizer {
    private final Trade plugin;
    private final Set<UUID> finalizing = new HashSet<>();

    public TradeFinalizer(Trade plugin) {
        this.plugin = plugin;
    }

    // NOVO MÉTODO: Para o TradeListener usar na trava de clique
    public boolean isFinalizing(UUID uuid) {
        return finalizing.contains(uuid);
    }

    public void processAccept(Player p, TradeSession session, Inventory inv) {
        if (p.getUniqueId().equals(session.getP1())) {
            session.setAcceptedP1(true);
        } else {
            session.setAcceptedP2(true);
        }

        if (session.hasP1Accepted() && session.hasP2Accepted()) {
            // BLOQUEIO IMEDIATO: Ativa a trava assim que o countdown começa
            finalizing.add(session.getP1());
            finalizing.add(session.getP2());

            p.sendMessage("§a§l✔ §aAmbos aceitaram! Finalizando em 3 segundos...");
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!session.hasP1Accepted() || !session.hasP2Accepted()) {
                        // Se alguém desmarcou, removemos a trava
                        finalizing.remove(session.getP1());
                        finalizing.remove(session.getP2());
                        return;
                    }
                    
                    if (plugin.getTradeManager().getSession(session.getP1()) == null) {
                        finalizing.remove(session.getP1());
                        finalizing.remove(session.getP2());
                        return;
                    }

                    checkAndComplete(session);
                }
            }.runTaskLater(plugin, 60L);
        } else {
            SoundManager.playSuccess(p);
        }
    }

    private void checkAndComplete(TradeSession s) {
        Player p1 = plugin.getServer().getPlayer(s.getP1());
        Player p2 = plugin.getServer().getPlayer(s.getP2());
        
        if (p1 == null || p2 == null) {
            cancelTrade(null, s);
            return;
        }

        if (plugin.getTradeChestManager().isChestFull(s.getP1()) || plugin.getTradeChestManager().isChestFull(s.getP2())) {
            cancelWithMsg(p1, p2, s, "§cTroca cancelada! O baú de trocas de um dos jogadores está lotado.");
            return;
        }

        // Já estão no finalizing, removemos do manager para processar
        plugin.getTradeManager().removeSession(s.getP1());
        executeTransaction(p1, p2, s);
    }

    private void executeTransaction(Player p1, Player p2, TradeSession s) {
        try {
            plugin.getIntegration().deposit(p2, s.getCoinsP1());
            plugin.getIntegration().deposit(p1, s.getCoinsP2());
            plugin.getIntegration().addClaimBlocks(p2, s.getBlocksP1());
            plugin.getIntegration().addClaimBlocks(p1, s.getBlocksP2());

            s.getItemsP1().values().forEach(i -> plugin.getTradeChestManager().addItemToChest(s.getP2(), i));
            s.getItemsP2().values().forEach(i -> plugin.getTradeChestManager().addItemToChest(s.getP1(), i));

            plugin.getLogManager().logTrade(p1, p2, s, "Success");

            p1.closeInventory();
            p2.closeInventory();
            p1.sendMessage(plugin.getMsgManager().getMessage("trade-success"));
            p2.sendMessage(plugin.getMsgManager().getMessage("trade-success"));
            SoundManager.playTradeDone(p1, p2);

        } finally {
            finalizing.remove(s.getP1());
            finalizing.remove(s.getP2());
        }
    }

    public void cancelTrade(Player p, TradeSession s) {
        // Se já removemos do manager por conta do finalize, não cancela por aqui
        if (plugin.getTradeManager().getSession(s.getP1()) == null && !finalizing.contains(s.getP1())) return;
        
        // Se a transação atômica iniciou, impossível cancelar
        if (plugin.getTradeManager().getSession(s.getP1()) == null) return;

        plugin.getTradeManager().removeSession(s.getP1());
        finalizing.remove(s.getP1());
        finalizing.remove(s.getP2());

        Player p1 = plugin.getServer().getPlayer(s.getP1());
        Player p2 = plugin.getServer().getPlayer(s.getP2());

        refund(p1, s.getCoinsP1(), s.getBlocksP1(), s.getItemsP1().values());
        refund(p2, s.getCoinsP2(), s.getBlocksP2(), s.getItemsP2().values());

        sendCancelMessages(p1, p2, p);
    }

    private void refund(Player p, double coins, int blocks, java.util.Collection<ItemStack> items) {
        if (p == null) return;
        plugin.getIntegration().deposit(p, coins);
        plugin.getIntegration().addClaimBlocks(p, blocks);
        for (ItemStack item : items) {
            if (p.getInventory().firstEmpty() == -1) {
                p.getWorld().dropItemNaturally(p.getLocation(), item);
            } else {
                p.getInventory().addItem(item);
            }
        }
        p.closeInventory();
    }

    private void cancelWithMsg(Player p1, Player p2, TradeSession s, String msg) {
        if (p1 != null) p1.sendMessage(msg);
        if (p2 != null) p2.sendMessage(msg);
        cancelTrade(null, s);
    }

    private void sendCancelMessages(Player p1, Player p2, Player causer) {
        MessageManager m = plugin.getMsgManager();
        if (p1 != null) {
            String msg = (causer != null && p1.equals(causer)) ? m.getMessage("trade-closed-self") : m.replace(m.getMessage("trade-closed-other"), "player", causer != null ? causer.getName() : "Sistema");
            p1.sendMessage(msg);
            SoundManager.playCancel(p1);
        }
        if (p2 != null) {
            String msg = (causer != null && p2.equals(causer)) ? m.getMessage("trade-closed-self") : m.replace(m.getMessage("trade-closed-other"), "player", causer != null ? causer.getName() : "Sistema");
            p2.sendMessage(msg);
            SoundManager.playCancel(p2);
        }
    }
}
