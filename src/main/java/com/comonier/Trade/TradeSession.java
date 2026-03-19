package com.comonier.Trade;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class TradeSession {
    private final UUID player1;
    private final UUID player2;
    
    private double coinsP1 = 0, coinsP2 = 0;
    private int blocksP1 = 0, blocksP2 = 0;
    private boolean acceptedP1 = false, acceptedP2 = false;
    
    private final Map<Integer, ItemStack> itemsP1 = new HashMap<>();
    private final Map<Integer, ItemStack> itemsP2 = new HashMap<>();

    public TradeSession(UUID p1, UUID p2) {
        this.player1 = p1;
        this.player2 = p2;
    }

    public void setItem(UUID uuid, int slot, ItemStack item) {
        if (uuid.equals(player1)) {
            itemsP1.put(slot, item);
        } else {
            itemsP2.put(slot, item);
        }
        resetAcceptance();
    }

    public void removeItem(UUID uuid, int slot) {
        if (uuid.equals(player1)) {
            itemsP1.remove(slot);
        } else {
            itemsP2.remove(slot);
        }
        resetAcceptance();
    }

    public void addCoins(UUID uuid, double amount) {
        if (uuid.equals(player1)) {
            coinsP1 += amount;
        } else {
            coinsP2 += amount;
        }
        resetAcceptance();
    }

    public void addBlocks(UUID uuid, int amount) {
        if (uuid.equals(player1)) {
            blocksP1 += amount;
        } else {
            blocksP2 += amount;
        }
        resetAcceptance();
    }

    public void resetAcceptance() {
        this.acceptedP1 = false;
        this.acceptedP2 = false;
    }

    public UUID getP1() { return player1; }
    public UUID getP2() { return player2; }
    public double getCoinsP1() { return coinsP1; }
    public double getCoinsP2() { return coinsP2; }
    public int getBlocksP1() { return blocksP1; }
    public int getBlocksP2() { return blocksP2; }
    public boolean hasP1Accepted() { return acceptedP1; }
    public boolean hasP2Accepted() { return acceptedP2; }
    public void setAcceptedP1(boolean val) { this.acceptedP1 = val; }
    public void setAcceptedP2(boolean val) { this.acceptedP2 = val; }
    public Map<Integer, ItemStack> getItemsP1() { return itemsP1; }
    public Map<Integer, ItemStack> getItemsP2() { return itemsP2; }
}
