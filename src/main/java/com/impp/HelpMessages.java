package com.impp;

import org.bukkit.command.CommandSender;

public final class HelpMessages {
    private HelpMessages() {
    }

    public static void sendImpGlobalHelp(CommandSender sender) {
        sender.sendMessage("impp plugin commands:");
        sender.sendMessage(" - /imp <username> <message>");
        sender.sendMessage(" - /impr <player> <message>");
        sender.sendMessage(" - /imprr <excluded...> <speaker>:<message>");
        sender.sendMessage(" - /imprr <excluded...> <speaker>::<message>");
        sender.sendMessage(" - /impp <username1> <username2>");
        sender.sendMessage("Use /<command> -help for command-specific help.");
    }

    public static void sendImpHelp(CommandSender sender) {
        sender.sendMessage("/imp help:");
        sender.sendMessage("Usage: /imp <username> <message>");
        sender.sendMessage("Sends message as <username>.");
        sender.sendMessage("For non-ops: only self or /impp-granted names.");
        sender.sendMessage("Console can use any known name.");
    }

    public static void sendImprHelp(CommandSender sender) {
        sender.sendMessage("/impr help:");
        sender.sendMessage("Usage: /impr <player> <message>");
        sender.sendMessage("Outputs as <player> <message>.");
        sender.sendMessage("That same <player> will NOT see the message.");
    }

    public static void sendImprrHelp(CommandSender sender) {
        sender.sendMessage("/imprr help:");
        sender.sendMessage("Usage: /imprr <excluded...> <speaker>:<message>");
        sender.sendMessage(" - speaker: excludes speaker from seeing message");
        sender.sendMessage("Usage: /imprr <excluded...> <speaker>::<message>");
        sender.sendMessage(" - speaker:: includes speaker");
    }

    public static void sendImppHelp(CommandSender sender) {
        sender.sendMessage("/impp help:");
        sender.sendMessage("Usage: /impp <username1> <username2>");
        sender.sendMessage("Grants username1 permission to impersonate username2.");
        sender.sendMessage("Requires op or impp.manage.");
    }
}
