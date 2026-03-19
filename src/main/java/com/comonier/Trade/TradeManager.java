package com.comonier.Trade;

import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TradeManager {
    private final Trade plugin;
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();
    private final Map<UUID, Long> inviteTime = new HashMap<>();
    private final Map<UUID, TradeSession> activeSessions = new HashMap<>();

    public TradeManager(Trade plugin) {
        this.plugin = plugin;
    }

    public void sendInvite(Player sender, Player receiver) {
        pendingInvites.put(receiver.getUniqueId(), sender.getUniqueId());
        inviteTime.put(sender.getUniqueId(), System.currentTimeMillis());
    }

    public boolean hasInvite(Player receiver) {
        UUID senderUUID = pendingInvites.get(receiver.getUniqueId());
        if (senderUUID == null) return false;

        long timestamp = inviteTime.getOrDefault(senderUUID, 0L);
        long timeout = plugin.getConfig().getLong("settings.request-timeout", 30) * 1000L;
        
        if (System.currentTimeMillis() - timestamp > timeout) {
            clearInvite(receiver);
            return false;
        }
        return true;
    }

    public UUID getSender(Player receiver) {
        return pendingInvites.get(receiver.getUniqueId());
    }

    public void clearInvite(Player receiver) {
        UUID senderUUID = pendingInvites.remove(receiver.getUniqueId());
        if (senderUUID != null) {
            inviteTime.remove(senderUUID);
        }
    }

    public void addSession(TradeSession session) {
        activeSessions.put(session.getP1(), session);
        activeSessions.put(session.getP2(), session);
        // Não remove o convite se for a mesma pessoa (evita erro de null no accept secundário)
        if (session.getP1().equals(session.getP2()) == false) {
            pendingInvites.remove(session.getP2());
        }
    }

    public void removeSession(UUID uuid) {
        TradeSession session = activeSessions.get(uuid);
        if (session != null) {
            activeSessions.remove(session.getP1());
            activeSessions.remove(session.getP2());
        }
    }

    public TradeSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    public Collection<TradeSession> getActiveSessions() {
        return activeSessions.values().stream().distinct().collect(Collectors.toList());
    }
}
