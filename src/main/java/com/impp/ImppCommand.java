package com.impp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ImppCommand implements CommandExecutor, TabCompleter {
    private final ImppPlugin plugin;
    private final PermissionStore permissionStore;

    public ImppCommand(ImppPlugin plugin, PermissionStore permissionStore) {
        this.plugin = plugin;
        this.permissionStore = permissionStore;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("-help")) {
            HelpMessages.sendImppHelp(sender);
            return true;
        }

        if (!sender.isOp() && !sender.hasPermission("impp.manage")) {
            sender.sendMessage("Only operators can use /impp.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /impp <username1> <username2>");
            return true;
        }

        String grantor = args[0].trim();
        String target = args[1].trim();
        if (grantor.isEmpty() || target.isEmpty()) {
            sender.sendMessage("Both usernames must be non-empty.");
            return true;
        }

        permissionStore.registerName(grantor);
        permissionStore.registerName(target);
        permissionStore.grant(grantor, target);
        permissionStore.save();

        sender.sendMessage(grantor + " can now impersonate " + target + ".");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {
        if (!sender.isOp() && !sender.hasPermission("impp.manage")) {
            return List.of();
        }

        if (args.length == 1 || args.length == 2) {
            String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
            Set<String> allNames = plugin.getAllKnownNames().stream().collect(Collectors.toSet());
            List<String> completions = new ArrayList<>();
            for (String name : allNames) {
                if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completions.add(name);
                }
            }
            completions.sort(String.CASE_INSENSITIVE_ORDER);
            return completions;
        }

        return List.of();
    }
}
