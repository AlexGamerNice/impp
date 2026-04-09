package com.impp;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImppPlugin extends JavaPlugin {
    private PermissionStore permissionStore;
    private ChatRouter chatRouter;

    @Override
    public void onEnable() {
        this.permissionStore = new PermissionStore(this);
        this.permissionStore.load();
        this.chatRouter = new ChatRouter(this);

        PluginCommand impCommand = getCommand("imp");
        PluginCommand imppCommand = getCommand("impp");
        if (impCommand == null || imppCommand == null) {
            getLogger().severe("Commands are missing from plugin.yml; disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ImpCommand impExecutor = new ImpCommand(permissionStore, chatRouter);
        ImppCommand imppExecutor = new ImppCommand(this, permissionStore);

        impCommand.setExecutor(impExecutor);
        impCommand.setTabCompleter(impExecutor);
        imppCommand.setExecutor(imppExecutor);
        imppCommand.setTabCompleter(imppExecutor);

        getServer().getPluginManager().registerEvents(new ChatRelayListener(this, chatRouter), this);
        getServer().getPluginManager().registerEvents(new PlayerNameCacheListener(permissionStore), this);
        getLogger().info("impp enabled.");
    }

    @Override
    public void onDisable() {
        if (permissionStore != null) {
            permissionStore.save();
        }
    }

    public List<String> getAllKnownNames() {
        SortedSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Player online : Bukkit.getOnlinePlayers()) {
            names.add(online.getName());
            permissionStore.registerName(online.getName());
        }
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.getName() != null && !offline.getName().isBlank()) {
                names.add(offline.getName());
                permissionStore.registerName(offline.getName());
            }
        }
        names.addAll(permissionStore.getAllCachedNames());
        return new ArrayList<>(names);
    }
}
