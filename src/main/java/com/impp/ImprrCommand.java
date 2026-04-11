package com.impp;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class ImprrCommand implements CommandExecutor, TabCompleter {
    private final PermissionStore permissionStore;
    private final ChatRouter chatRouter;

    public ImprrCommand(PermissionStore permissionStore, ChatRouter chatRouter) {
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
            sender.sendMessage("Usage: /imprr <excluded1> <excluded2> ... <speaker>: <message>");
            return true;
        }

        int colonIndex = findColonIndex(args);
        if (colonIndex < 0 || colonIndex >= args.length - 1) {
            sender.sendMessage("Usage: /imprr <excluded1> <excluded2> ... <speaker>: <message>");
            return true;
        }

        Set<String> excludedTargets = new LinkedHashSet<>();
        for (int i = 0; i < colonIndex; i++) {
            String excluded = args[i].trim();
            if (excluded.isEmpty()) {
                continue;
            }
            excludedTargets.add(excluded);
            permissionStore.registerName(excluded);
        }

        String speakerToken = args[colonIndex];
        String speakerName = speakerToken.substring(0, speakerToken.length() - 1).trim();
        if (speakerName.isEmpty()) {
            sender.sendMessage("Speaker name cannot be empty.");
            return true;
        }

        if (!canImpersonate(player, speakerName)) {
            sender.sendMessage("You are not allowed to impersonate " + speakerName + ".");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, colonIndex + 1, args.length)).trim();
        if (message.isEmpty()) {
            sender.sendMessage("Message cannot be empty.");
            return true;
        }

        permissionStore.registerName(speakerName);
        chatRouter.sendImpersonatedMessageExcept(speakerName, message, excludedTargets);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 0) {
            return List.of();
        }

        Set<String> allowed = permissionStore.getAllowedTargets(player.getName(), player.isOp());
        int colonIndex = findColonIndex(args);
        if (colonIndex == -1) {
            String lastToken = args[args.length - 1];
            Set<String> alreadyExcluded = collectExcluded(args, args.length - 2);
            List<String> plain = NameSuggestionUtil.suggest(allowed, lastToken, alreadyExcluded);
            List<String> withColon = NameSuggestionUtil.suggestWithColon(allowed, lastToken, alreadyExcluded);
            List<String> merged = new ArrayList<>(plain.size() + withColon.size());
            merged.addAll(plain);
            merged.addAll(withColon);
            return merged;
        }

        if (colonIndex == args.length - 1 && args[colonIndex].endsWith(":")) {
            return List.of();
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

    private int findColonIndex(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].endsWith(":")) {
                return i;
            }
        }
        return -1;
    }

    private Set<String> collectExcluded(String[] args, int maxIndex) {
        Set<String> excluded = new LinkedHashSet<>();
        int end = Math.min(maxIndex, args.length - 1);
        for (int i = 0; i <= end; i++) {
            String token = args[i];
            if (token.endsWith(":")) {
                break;
            }
            String name = token.trim();
            if (!name.isEmpty()) {
                excluded.add(name);
            }
        }
        return excluded;
    }
}
