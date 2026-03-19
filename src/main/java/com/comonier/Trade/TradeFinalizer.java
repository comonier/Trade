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
    // Trava de segurança para evitar cancelamento enquanto os itens são movidos
    private final Set<UUID> finalizing = new HashSet<>();

    public TradeFinalizer(Trade plugin) {
        this.plugin = plugin;
    }

    public void processAccept(Player p, TradeSession session, Inventory inv) {
        if (p.getUniqueId().equals(session.getP1())) {
            session.setAcceptedP1(true);
        } else {
            session.setAcceptedP2(true);
        }

        // Se ambos aceitaram, inicia contagem de segurança
        if (session.hasP1Accepted() && session.hasP2Accepted()) {
            p.sendMessage("§a§l✔ §aAmbos aceitaram! Finalizando em 3 segundos...");
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    // VERIFICAÇÃO DE SEGURANÇA: Alguém desmarcou ou fechou?
                    if (!session.hasP1Accepted() || !session.hasP2Accepted()) {
                        return; // Aborta silenciosamente (o status já resetou no listener)
                    }
                    
                    // Verifica se a sessão ainda é válida no manager
                    if (plugin.getTradeManager().getSession(session.getP1()) == null) return;

                    checkAndComplete(session);
                }
            }.runTaskLater(plugin, 60L);
        } else {
            // Apenas um aceitou, toca som de progresso
            SoundManager.playSuccess(p);
        }
    }

    private void checkAndComplete(TradeSession s) {
        Player p1 = plugin.getServer().getPlayer(s.getP1());
        Player p2 = plugin.getServer().getPlayer(s.getP2());
        
        // Se um saiu, cancela e devolve os itens
        if (p1 == null || p2 == null) {
            cancelTrade(null, s);
            return;
        }

        // Verifica espaço no baú virtual novamente (prevenção de última hora)
        if (plugin.getTradeChestManager().isChestFull(s.getP1()) || plugin.getTradeChestManager().isChestFull(s.getP2())) {
            cancelWithMsg(p1, p2, s, "§cTroca cancelada! O baú de trocas de um dos jogadores está lotado.");
            return;
        }

        // BLOQUEIO ATÔMICO: A partir daqui, a troca não pode mais ser cancelada
        finalizing.add(s.getP1());
        finalizing.add(s.getP2());

        // Remove do manager para que nenhum comando ou evento interfira mais
        plugin.getTradeManager().removeSession(s.getP1());
        
        executeTransaction(p1, p2, s);
    }

    private void executeTransaction(Player p1, Player p2, TradeSession s) {
        try {
            // 1. Transação de Coins (Vault)
            plugin.getIntegration().deposit(p2, s.getCoinsP1());
            plugin.getIntegration().deposit(p1, s.getCoinsP2());

            // 2. Transação de Blocos (GriefPrevention)
            plugin.getIntegration().addClaimBlocks(p2, s.getBlocksP1());
            plugin.getIntegration().addClaimBlocks(p1, s.getBlocksP2());

            // 3. Entrega de Itens Físicos (Para o Trade Chest)
            s.getItemsP1().values().forEach(i -> plugin.getTradeChestManager().addItemToChest(s.getP2(), i));
            s.getItemsP2().values().forEach(i -> plugin.getTradeChestManager().addItemToChest(s.getP1(), i));

            // 4. Logs (Discord e Arquivo)
            plugin.getLogManager().logTrade(p1, p2, s, "Success");

            // 5. Efeitos e Mensagens
            p1.closeInventory();
            p2.closeInventory();
            p1.sendMessage(plugin.getMsgManager().getMessage("trade-success"));
            p2.sendMessage(plugin.getMsgManager().getMessage("trade-success"));
            SoundManager.playTradeDone(p1, p2);

        } finally {
            // Limpa as travas
            finalizing.remove(s.getP1());
            finalizing.remove(s.getP2());
        }
    }

    public void cancelTrade(Player p, TradeSession s) {
        // Se a troca já estiver na fase de execução atômica, o cancelamento é negado
        if (finalizing.contains(s.getP1())) return;

        // Verifica se a sessão ainda existe para evitar double-refund
        if (plugin.getTradeManager().getSession(s.getP1()) == null) return;
        
        plugin.getTradeManager().removeSession(s.getP1());

        Player p1 = plugin.getServer().getPlayer(s.getP1());
        Player p2 = plugin.getServer().getPlayer(s.getP2());

        // Devolve tudo para os donos originais
        refund(p1, s.getCoinsP1(), s.getBlocksP1(), s.getItemsP1().values());
        refund(p2, s.getCoinsP2(), s.getBlocksP2(), s.getItemsP2().values());

        sendCancelMessages(p1, p2, p);
    }

    private void refund(Player p, double coins, int blocks, java.util.Collection<ItemStack> items) {
        if (p == null) return;
        plugin.getIntegration().deposit(p, coins);
        plugin.getIntegration().addClaimBlocks(p, blocks);
        for (ItemStack item : items) {
            // Se o inventário do player encher no refund, vai pro chão
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
