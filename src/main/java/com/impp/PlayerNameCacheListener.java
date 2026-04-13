package com.impp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerNameCacheListener implements Listener {
    private final PermissionStore store;

    public PlayerNameCacheListener(PermissionStore store) {
        this.store = store;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        store.registerName(event.getPlayer().getName());
    }
}
