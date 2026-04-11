package com.impp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PermissionStore {
    private static final String ROOT_PERMISSIONS = "permissions";
    private static final String ROOT_NAME_CACHE = "name-cache";

    private final JavaPlugin plugin;
    private final Map<String, Set<String>> permissionsByImpersonatorLower = new ConcurrentHashMap<>();
    private final Map<String, String> knownNamesByLower = new ConcurrentHashMap<>();
    private File file;

    public PermissionStore(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }
        file = new File(plugin.getDataFolder(), "permissions.yml");

        if (!file.exists()) {
            save();
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        permissionsByImpersonatorLower.clear();
        if (config.isConfigurationSection(ROOT_PERMISSIONS)) {
            for (String impersonator : config.getConfigurationSection(ROOT_PERMISSIONS).getKeys(false)) {
                List<String> targets = config.getStringList(ROOT_PERMISSIONS + "." + impersonator);
                String impersonatorName = normalize(impersonator);
                if (impersonatorName.isBlank()) {
                    continue;
                }
                registerName(impersonatorName);
                Set<String> targetSet = new LinkedHashSet<>();
                for (String target : targets) {
                    String targetName = normalize(target);
                    if (targetName.isBlank()) {
                        continue;
                    }
                    registerName(targetName);
                    targetSet.add(toKey(targetName));
                }
                if (!targetSet.isEmpty()) {
                    permissionsByImpersonatorLower.put(toKey(impersonatorName), targetSet);
                }
            }
        }

        for (String cachedName : config.getStringList(ROOT_NAME_CACHE)) {
            registerName(cachedName);
        }
    }

    public synchronized void save() {
        if (file == null) {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder.");
                return;
            }
            file = new File(plugin.getDataFolder(), "permissions.yml");
        }

        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Set<String>> entry : permissionsByImpersonatorLower.entrySet()) {
            String displayImpersonator = resolveDisplayName(entry.getKey());
            List<String> displayTargets = entry.getValue().stream()
                    .map(this::resolveDisplayName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toCollection(ArrayList::new));
            config.set(ROOT_PERMISSIONS + "." + displayImpersonator, displayTargets);
        }
        List<String> allKnownNames = knownNamesByLower.values().stream()
                .sorted(Comparator.comparing(name -> name.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toCollection(ArrayList::new));
        config.set(ROOT_NAME_CACHE, allKnownNames);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save permissions.yml: " + e.getMessage());
        }
    }

    public synchronized void grant(String impersonatorName, String targetName) {
        String impersonator = normalize(impersonatorName);
        String target = normalize(targetName);
        if (impersonator.isBlank() || target.isBlank()) {
            return;
        }
        registerName(impersonator);
        registerName(target);
        permissionsByImpersonatorLower.computeIfAbsent(toKey(impersonator), ignored -> new LinkedHashSet<>())
                .add(toKey(target));
        save();
    }

    public boolean canImpersonate(String impersonatorName, String targetName, boolean isOperator) {
        if (isOperator) {
            return true;
        }
        String impersonatorKey = toKey(impersonatorName);
        String targetKey = toKey(targetName);
        if (impersonatorKey.isBlank() || targetKey.isBlank()) {
            return false;
        }
        Set<String> allowed = permissionsByImpersonatorLower.get(impersonatorKey);
        return allowed != null && allowed.contains(targetKey);
    }

    public Set<String> getAllowedTargets(String impersonatorName, boolean isOperator) {
        if (isOperator) {
            return getAllKnownNames();
        }
        String impersonatorKey = toKey(impersonatorName);
        Set<String> allowed = permissionsByImpersonatorLower.getOrDefault(impersonatorKey, Collections.emptySet());
        return allowed.stream()
                .map(this::resolveDisplayName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getAllKnownNames() {
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.getName() != null) {
                registerName(offline.getName());
            }
        }
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            registerName(online.getName());
        }
        return new LinkedHashSet<>(knownNamesByLower.values());
    }

    public void registerName(String name) {
        String normalized = normalize(name);
        if (normalized.isBlank()) {
            return;
        }
        knownNamesByLower.put(toKey(normalized), normalized);
    }

    public Collection<String> getAllCachedNames() {
        return new LinkedHashSet<>(knownNamesByLower.values());
    }

    private String normalize(String name) {
        return name == null ? "" : name.trim();
    }

    private String toKey(String name) {
        return normalize(name).toLowerCase(Locale.ROOT);
    }

    private String resolveDisplayName(String lowerNameKey) {
        return knownNamesByLower.getOrDefault(lowerNameKey, lowerNameKey);
    }
}
