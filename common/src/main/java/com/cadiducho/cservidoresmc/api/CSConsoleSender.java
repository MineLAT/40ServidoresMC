package com.cadiducho.cservidoresmc.api;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CSConsoleSender implements CSCommandSender {

    private static final UUID UNIQUE_ID = new UUID(0, 0);
    private static final String NAME = "Console";

    private final CSPlugin plugin;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public UUID getUniqueId() {
        return UNIQUE_ID;
    }

    @Override
    public CSPlugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public void sendMessage(String message) {
        plugin.logMessage(5, message);
    }
}
