package com.comonier.Trade;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class TradeCommand implements CommandExecutor {
    private final Trade plugin;

    public TradeCommand(Trade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player == false) {
            sender.sendMessage(plugin.getMsgManager().getMessage("player-only"));
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(p);
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            handleReload(p);
        } else if (sub.equals("accept") || sub.equals("aceitar")) {
            handleAccept(p);
        } else if (sub.equals("chest") || sub.equals("bau")) {
            plugin.getTradeChestManager().openChest(p);
        } else {
            handleInvite(p, args[0]);
        }
        return true;
    }

    private void handleReload(Player p) {
        if (p.hasPermission("trade.admin") == false) {
            p.sendMessage(plugin.getMsgManager().getMessage("no-permission"));
            return;
        }
        plugin.reloadConfig();
        plugin.getMsgManager().loadMessages();
        p.sendMessage(plugin.getMsgManager().getMessage("reload-success"));
    }

    private void handleInvite(Player p, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            p.sendMessage(plugin.getMsgManager().getMessage("player-not-found"));
            return;
        }
        if (target.equals(p)) {
            p.sendMessage(plugin.getMsgManager().getMessage("trade-self"));
            return;
        }
        plugin.getTradeManager().sendInvite(p, target);
        p.sendMessage(plugin.getMsgManager().replace(plugin.getMsgManager().getMessage("trade-invited"), "player", target.getName()));
        target.sendMessage(plugin.getMsgManager().replace(plugin.getMsgManager().getMessage("trade-received"), "player", p.getName()));
    }

    private void handleAccept(Player p) {
        if (plugin.getTradeManager().hasInvite(p) == false) {
            p.sendMessage("§cVocê não tem convites pendentes ou ele expirou.");
            return;
        }
        
        UUID sID = plugin.getTradeManager().getSender(p);
        Player s = Bukkit.getPlayer(sID);
        
        if (s == null) {
            p.sendMessage(plugin.getMsgManager().getMessage("player-not-found"));
            return;
        }

        TradeSession session = new TradeSession(s.getUniqueId(), p.getUniqueId());
        plugin.getTradeManager().addSession(session);
        
        // Abre para ambos simultaneamente
        s.openInventory(TradeInventory.create(s, p, session));
        p.openInventory(TradeInventory.create(p, s, session));
        
        p.sendMessage(plugin.getMsgManager().getMessage("trade-accepted"));
        s.sendMessage(plugin.getMsgManager().getMessage("trade-accepted"));
    }

    private void sendHelp(Player p) {
        MessageManager m = plugin.getMsgManager();
        p.sendMessage(m.getRawMessage("help.header"));
        p.sendMessage(m.getRawMessage("help.line1"));
        p.sendMessage(m.getRawMessage("help.line2"));
        p.sendMessage(m.getRawMessage("help.line3"));
        p.sendMessage(m.getRawMessage("help.line4"));
    }
}
