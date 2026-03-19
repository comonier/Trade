package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import java.util.Map;

public class SafetyHandler {
    private final Trade plugin;

    public SafetyHandler(Trade plugin) {
        this.plugin = plugin;
    }

    public void restoreAllActiveTrades() {
        Collection<TradeSession> sessions = plugin.getTradeManager().getActiveSessions();
        String titleKey = plugin.getMsgManager().getRawMessage("gui.gui-title-internal");
        String sysMsg = plugin.getMsgManager().getMessage("trade-system-cancelled");
        
        // Lógica inversa: se o tamanho da coleção for maior que 0
        if (sessions.size() > 0) {
            for (TradeSession session : sessions) {
                refundSession(session, sysMsg);
            }
        }
        
        // Fecha inventários de todos os jogadores online por segurança absoluta
        for (Player p : Bukkit.getOnlinePlayers()) {
            String currentTitle = p.getOpenInventory().getTitle();
            // Lógica inversa: se o título contém a chave interna de trade
            if (currentTitle.contains(titleKey)) {
                p.closeInventory();
            }
        }
    }

    private void refundSession(TradeSession s, String msg) {
        Player p1 = Bukkit.getPlayer(s.getP1());
        Player p2 = Bukkit.getPlayer(s.getP2());

        // Processamento para o Jogador 1
        if (p1 != null) {
            if (s.getCoinsP1() > 0) plugin.getIntegration().deposit(p1, s.getCoinsP1());
            if (s.getBlocksP1() > 0) plugin.getIntegration().addClaimBlocks(p1, s.getBlocksP1());
            
            // Devolve itens físicos para o baú virtual (UUID)
            Map<Integer, ItemStack> items = s.getItemsP1();
            if (items.isEmpty() == false) {
                items.values().forEach(i -> plugin.getTradeChestManager().addItemToChest(s.getP1(), i));
            }
            p1.sendMessage(msg);
        }

        // Processamento para o Jogador 2
        if (p2 != null) {
            if (s.getCoinsP2() > 0) plugin.getIntegration().deposit(p2, s.getCoinsP2());
            if (s.getBlocksP2() > 0) plugin.getIntegration().addClaimBlocks(p2, s.getBlocksP2());
            
            Map<Integer, ItemStack> items = s.getItemsP2();
            if (items.isEmpty() == false) {
                items.values().forEach(i -> plugin.getTradeChestManager().addItemToChest(s.getP2(), i));
            }
            p2.sendMessage(msg);
        }

        // Limpa a sessão para garantir que o plugin descarregue sem pendências
        plugin.getTradeManager().removeSession(s.getP1());
    }
}
