package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TradeChestManager {
    private final Trade plugin;
    private final File file;
    private FileConfiguration config;

    public TradeChestManager(Trade plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "storage.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create storage.yml!");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void openChest(Player player) {
        String uuidStr = player.getUniqueId().toString();
        String title = plugin.getMsgManager().getRawMessage("gui.chest-title")
                .replace("{player}", player.getName());
        
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        List<?> items = config.getList(uuidStr);
        if (items != null) {
            ItemStack[] content = items.toArray(new ItemStack[0]);
            int limit = content.length;
            if (limit > 27) limit = 27;
            
            for (int i = 0; i < limit; i++) {
                inv.setItem(i, content[i]);
            }
        }
        player.openInventory(inv);
    }

    public void saveChest(Player player, Inventory inv) {
        String uuidStr = player.getUniqueId().toString();
        // Filtra para salvar apenas o que não é AR (vazio)
        List<ItemStack> toSave = Arrays.stream(inv.getContents())
                .filter(item -> item != null && item.getType().isAir() == false)
                .collect(Collectors.toList());
        
        config.set(uuidStr, toSave);
        saveToFile();
    }

    public void addItemToChest(UUID uuid, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        String uuidStr = uuid.toString();
        List<ItemStack> items = (List<ItemStack>) config.getList(uuidStr);
        
        if (items == null) {
            items = new ArrayList<>();
        }
        
        items.add(item.clone());
        config.set(uuidStr, items);
        saveToFile();
    }

    public boolean isChestFull(UUID uuid) {
        String uuidStr = uuid.toString();
        List<?> items = config.getList(uuidStr);
        if (items == null) return false;
        return items.size() >= 27;
    }

    private void saveToFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save storage.yml!");
        }
    }
}
