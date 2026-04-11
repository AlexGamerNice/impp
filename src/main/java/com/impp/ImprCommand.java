package com.impp;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class ImprCommand implements CommandExecutor, TabCompleter {
    private final PermissionStore permissionStore;
    private final ChatRouter chatRouter;

    public ImprCommand(PermissionStore permissionStore, ChatRouter chatRouter) {
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
            sender.sendMessage("Usage: /impr <player> <message>");
            return true;
        }

        String targetPlayer = args[0].trim();
        if (targetPlayer.isEmpty()) {
            sender.sendMessage("Player name cannot be empty.");
            return true;
        }

        if (!canImpersonate(player, targetPlayer)) {
            sender.sendMessage("You are not allowed to impersonate " + targetPlayer + ".");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (message.isEmpty()) {
            sender.sendMessage("Message cannot be empty.");
            return true;
        }

        permissionStore.registerName(targetPlayer);
        chatRouter.sendImpersonatedMessageExcluding(targetPlayer, message, Set.of(targetPlayer));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        Set<String> allowedNames = permissionStore.getAllowedTargets(player.getName(), player.isOp());

        if (args.length == 1) {
            return NameSuggestionUtil.suggest(allowedNames, args[0], Set.of());
        }
        return List.of();
    }

    private boolean canImpersonate(Player player, String requestedTarget) {
        if (player.isOp()) {
            return true;
        }
        if (player.getName().equalsIgnoreCase(requestedTarget)) {
            return true;
        }
        return permissionStore.canImpersonate(player.getName(), requestedTarget, false);
    }
}
