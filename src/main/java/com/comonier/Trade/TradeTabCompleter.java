package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TradeTabCompleter implements TabCompleter {

    private final List<String> SUB_COMMANDS = Arrays.asList(
        "accept", "aceitar", "chest", "bau", "help", "reload"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        // Apenas jogadores recebem sugestões (Lógica inversa: se não for player, retorna lista vazia)
        if (sender instanceof Player == false) return new ArrayList<>();

        List<String> suggestions = new ArrayList<>();

        // Primeiro argumento: /trade <subcomando|jogador>
        if (args.length == 1) {
            // Adiciona sub-comandos básicos
            suggestions.addAll(SUB_COMMANDS);
            
            // Adiciona nomes de todos os jogadores online (Java e Bedrock)
            for (Player online : Bukkit.getOnlinePlayers()) {
                // Não sugere o próprio nome para troca
                if (online.getName().equalsIgnoreCase(sender.getName()) == false) {
                    suggestions.add(online.getName());
                }
            }

            // Filtra o que o jogador já começou a digitar
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Não há sugestões para segundo argumento ou superior
        return new ArrayList<>();
    }
}
