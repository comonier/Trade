package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeInventory {

    public static Inventory create(Player p1, Player p2, TradeSession session) {
        MessageManager msg = Trade.getInstance().getMsgManager();
        String title = msg.getRawMessage("gui.title").replace("{player}", p2.getName());
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null, false);
        for (int i = 0; i != 54; i++) {
            int col = i % 9;
            if (col == 4 || i < 9 || col == 0 || col == 8 || i > 44) {
                inv.setItem(i, glass);
            }
        }
        updateButtons(inv, session);
        return inv;
    }

    public static void updateButtons(Inventory inv, TradeSession s) {
        Player p1 = Bukkit.getPlayer(s.getP1());
        Player p2 = Bukkit.getPlayer(s.getP2());

        if (p1 != null) inv.setItem(0, createSkull(p1));
        if (p2 != null) inv.setItem(8, createSkull(p2));

        // Lado P1: Inicia na Coluna 1
        setupSide(inv, 1, s.getCoinsP1(), s.getBlocksP1(), s.hasP1Accepted(), s.hasP2Accepted(), 45, 46, 47, 48);
        // Lado P2: Inicia na Coluna 5
        setupSide(inv, 5, s.getCoinsP2(), s.getBlocksP2(), s.hasP2Accepted(), s.hasP1Accepted(), 53, 52, 51, 50);

        clearTradeSlots(inv);
        for (Map.Entry<Integer, ItemStack> entry : s.getItemsP1().entrySet()) inv.setItem(entry.getKey(), entry.getValue());
        for (Map.Entry<Integer, ItemStack> entry : s.getItemsP2().entrySet()) inv.setItem(entry.getKey(), entry.getValue());
    }

    private static void clearTradeSlots(Inventory inv) {
        for (int row = 1; row <= 4; row++) {
            for (int col : new int[]{1, 2, 6, 7}) {
                inv.setItem((row * 9) + col, null);
            }
        }
    }

    private static void setupSide(Inventory inv, int startCol, double coins, int blocks, boolean meOk, boolean otherOk, int acceptSlot, int statusSlot, int cancelSlot, int updateSlot) {
        MessageManager m = Trade.getInstance().getMsgManager();
        int toolCol = (startCol == 1) ? 3 : 5;

        inv.setItem(acceptSlot, createItem(Material.GREEN_TERRACOTTA, m.getRawMessage("gui.accept-button"), null, false));
        
        Material statusMat;
        String statusMsg;
        
        if (meOk && otherOk) {
            statusMat = Material.LIME_STAINED_GLASS_PANE;
            statusMsg = m.getRawMessage("gui.accept-status-on");
        } else if (meOk) {
            statusMat = Material.LIME_STAINED_GLASS_PANE;
            statusMsg = m.getRawMessage("gui.accept-status-on");
        } else if (otherOk) {
            statusMat = Material.ORANGE_STAINED_GLASS_PANE;
            statusMsg = "§6Aguardando sua confirmação...";
        } else {
            statusMat = Material.YELLOW_STAINED_GLASS_PANE;
            statusMsg = m.getRawMessage("gui.accept-status-off");
        }

        inv.setItem(statusSlot, createItem(statusMat, statusMsg, null, meOk));
        inv.setItem(cancelSlot, createItem(Material.RED_STAINED_GLASS_PANE, m.getRawMessage("gui.cancel-button"), null, false));
        
        // BOTÃO ATUALIZADO: Passando 'true' para ocultar a lore do encantamento
        List<String> updateLore = new ArrayList<>();
        updateLore.add("§7Clique para atualizar a visão");
        updateLore.add("§7e ver mudanças do outro jogador.");
        inv.setItem(updateSlot, createItem(Material.ENDER_EYE, "§b§lATUALIZAR VISÃO", updateLore, true));

        String cText = (coins > 0) ? "§a+" + coins : "0";
        String bText = (blocks > 0) ? "§a+" + blocks : "0";

        List<String> cLore = new ArrayList<>();
        cLore.add(m.getRawMessage("gui.coins-current").replace("{amount}", cText));
        inv.setItem(18 + toolCol, createItem(Material.EMERALD, m.getRawMessage("gui.coins-add"), cLore, coins > 0));
        inv.setItem(27 + toolCol, createItem(Material.REDSTONE, m.getRawMessage("gui.coins-remove"), null, false));

        List<String> bLore = new ArrayList<>();
        bLore.add(m.getRawMessage("gui.claims-current").replace("{amount}", bText));
        inv.setItem(9 + toolCol, createItem(Material.GOLDEN_SHOVEL, m.getRawMessage("gui.claims-add"), bLore, blocks > 0));
        inv.setItem(toolCol + 36, createItem(Material.WOODEN_SHOVEL, m.getRawMessage("gui.claims-remove"), null, false));
    }

    private static ItemStack createSkull(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(p);
            meta.setDisplayName("§e" + p.getName());
            head.setItemMeta(meta);
        }
        return head;
    }

    private static ItemStack createItem(Material mat, String name, List<String> lore, boolean enchant) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            if (enchant) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
