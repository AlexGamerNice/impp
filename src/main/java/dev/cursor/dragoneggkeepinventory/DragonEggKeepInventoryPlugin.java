package dev.cursor.dragoneggkeepinventory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class DragonEggKeepInventoryPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DragonEggKeepInventory enabled.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> dragonEggDrops = new ArrayList<>();

        ItemStack[] allItems = inventory.getContents();
        for (int slot = 0; slot < allItems.length; slot++) {
            ItemStack item = allItems[slot];
            if (item == null || item.getType() != Material.DRAGON_EGG) {
                continue;
            }

            dragonEggDrops.add(item.clone());
            inventory.setItem(slot, null);
        }

        event.setKeepInventory(true);
        event.getDrops().clear();
        for (ItemStack dragonEgg : dragonEggDrops) {
            player.getWorld().dropItemNaturally(player.getLocation(), dragonEgg);
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
}
