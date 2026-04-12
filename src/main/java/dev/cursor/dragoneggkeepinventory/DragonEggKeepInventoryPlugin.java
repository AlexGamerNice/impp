package dev.cursor.dragoneggkeepinventory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DragonEggKeepInventoryPlugin extends JavaPlugin implements Listener {
    private static final long END_REENTRY_COOLDOWN_MS = 30_000L;
    private static final long MESSAGE_COOLDOWN_MS = 1_500L;

    private final Map<UUID, Long> endEntryBlockedUntil = new HashMap<>();
    private final Map<UUID, Long> lastCooldownMessageAt = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DragonEggKeepInventory enabled.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> forcedDrops = new ArrayList<>();
        UUID playerId = player.getUniqueId();

        ItemStack[] allItems = inventory.getContents();
        for (int slot = 0; slot < allItems.length; slot++) {
            ItemStack item = allItems[slot];
            if (item == null || !shouldDropOnDeath(item)) {
                continue;
            }

            forcedDrops.add(item.clone());
            inventory.setItem(slot, null);
        }

        event.setKeepInventory(true);
        event.getDrops().clear();
        for (ItemStack forcedDrop : forcedDrops) {
            player.getWorld().dropItemNaturally(player.getLocation(), forcedDrop);
        }

        if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
            endEntryBlockedUntil.put(playerId, System.currentTimeMillis() + END_REENTRY_COOLDOWN_MS);
            lastCooldownMessageAt.remove(playerId);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack.getType() != Material.DRAGON_EGG) {
            return;
        }

        Bukkit.broadcastMessage(
                ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.GOLD + " now has the Dragon Egg!"
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != TeleportCause.END_PORTAL || event.getTo() == null || event.getTo().getWorld() == null) {
            return;
        }

        if (event.getTo().getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }

        Player player = event.getPlayer();
        long remainingMs = getRemainingEndCooldownMs(player.getUniqueId());
        if (remainingMs <= 0) {
            return;
        }

        event.setCancelled(true);
        pushOutOfPortal(player);
        maybeSendCooldownMessage(player, remainingMs);
    }

    private long getRemainingEndCooldownMs(UUID playerId) {
        long now = System.currentTimeMillis();
        Long blockedUntil = endEntryBlockedUntil.get(playerId);
        if (blockedUntil == null) {
            return 0L;
        }

        long remaining = blockedUntil - now;
        if (remaining <= 0) {
            endEntryBlockedUntil.remove(playerId);
            lastCooldownMessageAt.remove(playerId);
            return 0L;
        }

        return remaining;
    }

    private void maybeSendCooldownMessage(Player player, long remainingMs) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        long lastMessage = lastCooldownMessageAt.getOrDefault(playerId, 0L);
        if (now - lastMessage < MESSAGE_COOLDOWN_MS) {
            return;
        }

        long remainingSeconds = Math.max(1L, (long) Math.ceil(remainingMs / 1000.0));
        player.sendMessage(
                ChatColor.RED + "You cannot jump into The End for "
                        + remainingSeconds
                        + " more second"
                        + (remainingSeconds == 1 ? "" : "s")
                        + "."
        );
        lastCooldownMessageAt.put(playerId, now);
    }

    private void pushOutOfPortal(Player player) {
        Vector pushVector = player.getLocation().getDirection().multiply(-0.8);
        if (pushVector.lengthSquared() < 0.01) {
            pushVector = new Vector(0.0, 0.0, 0.8);
        }
        pushVector.setY(0.35);
        player.setVelocity(pushVector);
    }

    private boolean shouldDropOnDeath(ItemStack item) {
        return item.getType() == Material.DRAGON_EGG || item.getType() == Material.SPLASH_POTION;
    }
}
