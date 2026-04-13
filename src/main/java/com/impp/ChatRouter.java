package com.impp;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatRouter {
    private final JavaPlugin plugin;

    public ChatRouter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void relayChatMessage(String senderName, String message) {
        String formatted = "<" + senderName + "> " + message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(formatted);
        }
        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    public void sendImpersonatedMessage(String senderName, String rawMessage) {
        relayChatMessage(senderName, rawMessage);
    }

    public void sendImpersonatedMessageExcluding(String senderName, String rawMessage, Set<String> excludedNames) {
        sendImpersonatedMessageExcept(senderName, rawMessage, excludedNames);
    }

    public void sendImpersonatedMessageExcept(String senderName, String rawMessage, Set<String> excludedNames) {
        String formatted = "<" + senderName + "> " + rawMessage;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isExcluded(player.getName(), excludedNames)) {
                continue;
            }
            player.sendMessage(formatted);
        }
        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    private boolean isExcluded(String playerName, Set<String> excludedNames) {
        for (String excluded : excludedNames) {
            if (excluded.equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }
}
