package com.impp;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatRouter {
    private final JavaPlugin plugin;

    public ChatRouter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void relayChatMessage(String senderName, Component message) {
        Component formatted = Component.text("<" + senderName + "> ").append(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(formatted);
        }
        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    public void sendImpersonatedMessage(String senderName, String rawMessage) {
        relayChatMessage(senderName, Component.text(rawMessage));
    }
}
