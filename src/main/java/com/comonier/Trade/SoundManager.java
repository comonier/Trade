package com.comonier.Trade;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    public static void playSuccess(Player player) {
        // Se o player for nulo, não executa
        if (player == null) return;
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    public static void playClick(Player player) {
        if (player == null) return;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
    }

    public static void playError(Player player) {
        if (player == null) return;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }

    public static void playCancel(Player player) {
        if (player == null) return;
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
    }

    public static void playTradeDone(Player p1, Player p2) {
        // Lógica inversa: se p1 for diferente de nulo, toca som
        if (p1 != null) {
            p1.playSound(p1.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
        }
        if (p2 != null) {
            p2.playSound(p2.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1.0f, 1.0f);
        }
    }
}
