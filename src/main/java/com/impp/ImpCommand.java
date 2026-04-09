package com.impp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class ImpCommand implements CommandExecutor, TabCompleter {
    private final PermissionStore permissionStore;
    private final ChatRouter chatRouter;

    public ImpCommand(PermissionStore permissionStore, ChatRouter chatRouter) {
        this.permissionStore = permissionStore;
        this.chatRouter = chatRouter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /imp <username> <message>");
            return true;
        }

        String targetName = args[0].trim();
        if (targetName.isEmpty()) {
            sender.sendMessage("Username cannot be empty.");
            return true;
        }

        if (!canImpersonate(player, targetName)) {
            sender.sendMessage("You are not allowed to impersonate " + targetName + ".");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (message.isEmpty()) {
            sender.sendMessage("Message cannot be empty.");
            return true;
        }

        permissionStore.registerName(targetName);
        chatRouter.sendImpersonatedMessage(targetName, message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            Set<String> allowed = permissionStore.getAllowedTargets(player.getName(), player.isOp());
            List<String> completions = new ArrayList<>();
            for (String candidate : allowed) {
                if (candidate.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    completions.add(candidate);
                }
            }
            completions.sort(String.CASE_INSENSITIVE_ORDER);
            return completions;
        }
        return Collections.emptyList();
    }

    private boolean canImpersonate(Player player, String requestedTarget) {
        if (player.isOp()) {
            return true;
        }
        if (player.getName().equalsIgnoreCase(requestedTarget)) {
            return true;
        }
        for (String allowed : permissionStore.getAllowedTargets(player.getName(), false)) {
            if (allowed.equalsIgnoreCase(requestedTarget)) {
                return true;
            }
        }
        return false;
    }
}
