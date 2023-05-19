package com.cadiducho.cservidoresmc.bukkit;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class BukkitCommandSender implements CSCommandSender {

    private final Player player;
    private final BukkitPlugin plugin;

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public BukkitPlugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(plugin.color(message));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (BukkitPlugin.SERVER_VERSION <= 8) {
            player.sendTitle(plugin.color(title), plugin.color(subtitle));
        } else {
            player.sendTitle(plugin.color(title), plugin.color(subtitle), fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void sendActionbar(String text) {
        if (BukkitPlugin.SPIGOT_SERVER) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(plugin.color(text)));
        }
    }

    @Override
    public String parse(String text, Object... args) {
        final String result = CSCommandSender.super.parse(text, args);
        if (result == null) {
            return null;
        }
        if (plugin.isPapiEnabled()) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return result;
    }
}
