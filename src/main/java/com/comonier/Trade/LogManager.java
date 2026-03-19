package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

public class LogManager {
    private final Trade plugin;

    public LogManager(Trade plugin) {
        this.plugin = plugin;
    }

    public void logTrade(Player p1, Player p2, TradeSession s, String status) {
        String itemsP1 = formatItems(s.getItemsP1());
        String itemsP2 = formatItems(s.getItemsP2());

        StringBuilder yamlBody = new StringBuilder();
        yamlBody.append("```yaml\n");
        yamlBody.append(p1.getName()).append(" -> ").append(p2.getName()).append("\n");
        yamlBody.append("- items: ").append(itemsP1).append("\n");
        yamlBody.append("- coins: ").append((int)s.getCoinsP1()).append("\n");
        yamlBody.append("- claimblocks: ").append(s.getBlocksP1()).append("\n\n");

        yamlBody.append(p2.getName()).append(" -> ").append(p1.getName()).append("\n");
        yamlBody.append("- items: ").append(itemsP2).append("\n");
        yamlBody.append("- coins: ").append((int)s.getCoinsP2()).append("\n");
        yamlBody.append("- claimblocks: ").append(s.getBlocksP2()).append("\n");
        yamlBody.append("```");

        String fullMessage = "Tradelog: P1: " + p1.getName() + " & P2: " + p2.getName() + "\n" + yamlBody.toString();

        if (plugin.getConfig().getBoolean("logging.log-to-file", true)) {
            saveToFile(p1, p2, fullMessage);
        }

        if (plugin.getConfig().getBoolean("settings.discord-webhook-enabled")) {
            sendDiscordWebhook(fullMessage);
        }
    }

    private String formatItems(Map<Integer, ItemStack> items) {
        if (items == null || items.isEmpty()) return "empty";
        return items.values().stream()
                .filter(i -> i != null && !i.getType().isAir())
                .map(i -> i.getType().name().toLowerCase() + ":" + i.getAmount())
                .collect(Collectors.joining(", "));
    }

    private void saveToFile(Player p1, Player p2, String content) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String fileName = "trade_" + dtf.format(LocalDateTime.now()) + ".txt";
        File logFile = new File(plugin.getDataFolder() + "/logs", fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(content.replace("```yaml", "").replace("```", ""));
            writer.println("-------------------------------------------");
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao salvar log em arquivo!");
        }
    }

    private void sendDiscordWebhook(String content) {
        String webhookUrl = plugin.getConfig().getString("settings.discord-webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("discord.com") == false) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                // CORREÇÃO: Escapando quebras de linha e aspas para JSON válido
                String jsonSafeContent = content
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r");

                String json = "{\"content\": \"" + jsonSafeContent + "\"}";

                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = con.getResponseCode();
                if (responseCode >= 400) {
                    plugin.getLogger().warning("Discord Webhook erro " + responseCode + ". Verifique o URL ou formato.");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Falha ao enviar Webhook: " + e.getMessage());
            }
        });
    }
}
