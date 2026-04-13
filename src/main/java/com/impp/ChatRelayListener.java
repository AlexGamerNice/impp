package com.impp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatRelayListener implements Listener {
    private final ImppPlugin plugin;
    private final ChatRouter chatRouter;

    public ChatRelayListener(ImppPlugin plugin, ChatRouter chatRouter) {
        this.plugin = plugin;
        this.chatRouter = chatRouter;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        String sender = event.getPlayer().getName();
        String message = event.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, () -> chatRouter.relayChatMessage(sender, message));
    }
}
