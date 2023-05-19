package com.cadiducho.cservidoresmc.sponge;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.title.Title;

import java.util.UUID;

@RequiredArgsConstructor
public class SpongeCommandSender implements CSCommandSender {

    private final Player player;

    private final SpongePlugin plugin;

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public SpongePlugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(plugin.colorText(message));
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(Title.builder()
                .title(plugin.colorText(title != null ? title : ""))
                .subtitle(plugin.colorText(subtitle != null ? subtitle : ""))
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut)
                .build());
    }

    @Override
    public void sendActionbar(String text) {
        player.sendMessage(ChatTypes.ACTION_BAR, plugin.colorText(text));
    }
}
