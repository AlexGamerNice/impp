package com.impp;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatRelayListener implements Listener {
    private final ImppPlugin plugin;
    private final ChatRouter chatRouter;

    public ChatRelayListener(ImppPlugin plugin, ChatRouter chatRouter) {
        this.plugin = plugin;
        this.chatRouter = chatRouter;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        event.setCancelled(true);
        String sender = event.getPlayer().getName();
        Component message = event.message();
        plugin.getServer().getScheduler().runTask(plugin, () -> chatRouter.relayChatMessage(sender, message));
    }
}
