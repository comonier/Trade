package com.comonier.Trade;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessageManager {
    private FileConfiguration langConfig;
    private final Trade plugin;

    public MessageManager(Trade plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        String lang = plugin.getConfig().getString("settings.language", "en");
        File langFile = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        
        // Lógica inversa: se o arquivo não existe, tenta o padrão em inglês
        if (langFile.exists() == false) {
            langFile = new File(plugin.getDataFolder(), "messages_en.yml");
        }

        // Carrega o arquivo externo com suporte a UTF-8
        this.langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        // Se o arquivo externo estiver vazio, tenta carregar do recurso interno (jar)
        if (langConfig.getKeys(false).isEmpty()) {
            try (InputStreamReader reader = new InputStreamReader(
                    plugin.getResource("messages_" + lang + ".yml"), StandardCharsets.UTF_8)) {
                this.langConfig = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not load internal resource for language: " + lang);
            }
        }
    }

    public String getMessage(String path) {
        String message = langConfig.getString(path);
        if (message == null) return "§cMessage path not found: " + path;
        
        String prefix = langConfig.getString("prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getRawMessage(String path) {
        String message = langConfig.getString(path);
        // Lógica inversa: se for nulo, retorna o próprio path para não dar erro
        if (message == null) return path;
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String replace(String message, String placeholder, String value) {
        if (message == null) return "";
        return message.replace("{" + placeholder + "}", value);
    }
}
