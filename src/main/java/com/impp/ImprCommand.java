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
            sender.sendMessage("Usage: /impr <hidden-player> [display-player] <message>");
            return true;
        }

        String hiddenPlayer = args[0].trim();
        if (hiddenPlayer.isEmpty()) {
            sender.sendMessage("Hidden player name cannot be empty.");
            return true;
        }

        String displayPlayer;
        int messageStartIndex;
        String secondToken = args[1].trim();
        if (args.length >= 3 && isKnownName(secondToken) && canImpersonate(player, secondToken)) {
            displayPlayer = secondToken;
            messageStartIndex = 2;
        } else {
            displayPlayer = player.getName();
            messageStartIndex = 1;
        }

        if (!canImpersonate(player, displayPlayer)) {
            sender.sendMessage("You are not allowed to impersonate " + displayPlayer + ".");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, messageStartIndex, args.length)).trim();
        if (message.isEmpty()) {
            sender.sendMessage("Message cannot be empty.");
            return true;
        }

        permissionStore.registerName(hiddenPlayer);
        permissionStore.registerName(displayPlayer);
        chatRouter.sendImpersonatedMessageExcluding(displayPlayer, message, Set.of(hiddenPlayer));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        Set<String> allowedDisplayNames = permissionStore.getAllowedTargets(player.getName(), player.isOp());
        Set<String> allNames = permissionStore.getAllKnownNames();

        if (args.length == 1) {
            return NameSuggestionUtil.suggest(allNames, args[0], Set.of());
        }
        if (args.length == 2) {
            return NameSuggestionUtil.suggest(allowedDisplayNames, args[1], Set.of());
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

    private boolean isKnownName(String candidate) {
        for (String known : permissionStore.getAllKnownNames()) {
            if (known.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }
}
