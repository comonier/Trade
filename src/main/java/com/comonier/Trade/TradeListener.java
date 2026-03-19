package com.comonier.Trade;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class TradeListener implements Listener {
    private final Trade plugin;

    public TradeListener(Trade plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String tradeKey = plugin.getMsgManager().getRawMessage("gui.gui-title-internal");
        
        if (!title.contains(tradeKey)) return;

        event.setCancelled(true);
        Player p = (Player) event.getWhoClicked();

        if (event.getClick().isShiftClick() || event.getClick().isKeyboardClick()) {
            p.updateInventory();
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        TradeSession s = plugin.getTradeManager().getSession(p.getUniqueId());
        if (s == null) return;

        if (clicked.equals(event.getView().getBottomInventory())) {
            handleMoveToTrade(p, event, s, event.getView().getTopInventory());
        } 
        else if (clicked.equals(event.getView().getTopInventory())) {
            handleMenuClick(p, event, s, event.getView().getTopInventory());
        }
    }

    private void handleMoveToTrade(Player p, InventoryClickEvent event, TradeSession s, Inventory top) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        int slot = getFreeSlot(p.getUniqueId(), s, top);
        if (slot > -1) {
            s.resetAcceptance(); // Reset pois houve mudança de item
            s.setItem(p.getUniqueId(), slot, item.clone());
            event.setCurrentItem(null);
            plugin.getTradeSyncManager().sync(s, top);
        }
    }

    private void handleMenuClick(Player p, InventoryClickEvent event, TradeSession s, Inventory top) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;
        
        Material mat = item.getType();
        int slot = event.getSlot();
        UUID pID = p.getUniqueId();

        // LOGICA DO OLHO DO FIM (UPDATE VISUAL)
        if (mat == Material.ENDER_EYE) {
            // Só permite clicar no seu próprio botão de update
            if ((slot == 48 && pID.equals(s.getP1())) || (slot == 50 && pID.equals(s.getP2()))) {
                // AQUI NÃO DAMOS RESET ACCEPTANCE
                plugin.getTradeSyncManager().sync(s, top);
                SoundManager.playSuccess(p);
            }
            return;
        }

        if (mat == Material.GREEN_TERRACOTTA) {
            if ((slot == 45 && pID.equals(s.getP1())) || (slot == 53 && pID.equals(s.getP2()))) {
                plugin.getTradeFinalizer().processAccept(p, s, top);
                plugin.getTradeSyncManager().sync(s, top);
            }
            return;
        }

        if (mat == Material.RED_STAINED_GLASS_PANE) {
            if ((slot == 47 && pID.equals(s.getP1())) || (slot == 51 && pID.equals(s.getP2()))) {
                plugin.getTradeFinalizer().cancelTrade(p, s);
            }
            return;
        }

        if (isActionItem(mat)) {
            int col = slot % 9;
            if (((col == 3) && pID.equals(s.getP1())) || ((col == 5) && pID.equals(s.getP2()))) {
                s.resetAcceptance(); // Reset pois mudou valores
                processValueButtons(p, mat, s);
                plugin.getTradeSyncManager().sync(s, top);
            }
            return;
        }

        if (isTradeSlot(pID, s, slot)) {
            s.resetAcceptance(); // Reset pois retirou item
            p.getInventory().addItem(item.clone());
            s.removeItem(pID, slot);
            top.setItem(slot, null);
            plugin.getTradeSyncManager().sync(s, top);
        }
    }

    private int getFreeSlot(UUID id, TradeSession s, Inventory top) {
        int[] cols = id.equals(s.getP1()) ? new int[]{1, 2} : new int[]{6, 7};
        for (int row = 1; row <= 4; row++) {
            for (int col : cols) {
                int slot = (row * 9) + col;
                ItemStack cur = top.getItem(slot);
                if (cur == null || cur.getType().isAir()) return slot;
            }
        }
        return -1;
    }

    private boolean isTradeSlot(UUID id, TradeSession s, int slot) {
        int col = slot % 9;
        int row = slot / 9;
        if (row >= 1 && row <= 4) {
            if (id.equals(s.getP1())) return (col == 1 || col == 2);
            return (col == 6 || col == 7);
        }
        return false;
    }

    private boolean isActionItem(Material m) {
        return m == Material.EMERALD || m == Material.REDSTONE || 
               m == Material.GOLDEN_SHOVEL || m == Material.WOODEN_SHOVEL;
    }

    private void processValueButtons(Player p, Material mat, TradeSession s) {
        IntegrationManager integ = plugin.getIntegration();
        UUID id = p.getUniqueId();
        if (mat == Material.EMERALD && integ.getBalance(p) >= 100) {
            integ.withdraw(p, 100); s.addCoins(id, 100);
        } else if (mat == Material.REDSTONE) {
            double cur = id.equals(s.getP1()) ? s.getCoinsP1() : s.getCoinsP2();
            if (cur >= 100) { s.addCoins(id, -100); integ.deposit(p, 100); }
        } else if (mat == Material.GOLDEN_SHOVEL && integ.getRemainingBlocks(p) >= 100) {
            integ.removeClaimBlocks(p, 100); s.addBlocks(id, 100);
        } else if (mat == Material.WOODEN_SHOVEL) {
            int curB = id.equals(s.getP1()) ? s.getBlocksP1() : s.getBlocksP2();
            if (curB >= 100) { s.addBlocks(id, -100); integ.addClaimBlocks(p, 100); }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        String tradeKey = plugin.getMsgManager().getRawMessage("gui.gui-title-internal");
        String chestFullKey = plugin.getMsgManager().getRawMessage("gui.chest-title");
        int dashIndex = chestFullKey.indexOf(" -");
        String chestKeyBase = (dashIndex != -1) ? chestFullKey.substring(0, dashIndex) : chestFullKey;

        if (title.contains(tradeKey)) {
            TradeSession s = plugin.getTradeManager().getSession(event.getPlayer().getUniqueId());
            if (s != null) plugin.getTradeFinalizer().cancelTrade((Player) event.getPlayer(), s);
        } 
        else if (title.contains(chestKeyBase)) {
            plugin.getTradeChestManager().saveChest((Player) event.getPlayer(), event.getInventory());
        }
    }
}
