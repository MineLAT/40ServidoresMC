package com.cadiducho.cservidoresmc.api;

import com.cadiducho.cservidoresmc.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface CSCommandSender {

    /**
     * Get the name of the sender
     * @return The name of the sender
     */
    String getName();

    /**
     * Get the sender uuid
     * @return The unique of sender
     */
    UUID getUniqueId();

    /**
     * Get teh current plugin instance
     * @return a plugin
     */
    CSPlugin getPlugin();

    /**
     * Check if the sender is a console
     * @return true if the sender is a console
     */
    default boolean isConsole() {
        return false;
    }

    /**
     * Check if the sender has a permission
     * @param permission The permission
     * @return true if the sender has the permission
     */
    boolean hasPermission(String permission);

    /**
     * Send a message to the sender
     * @param message The message
     */
    void sendMessage(String message);

    /**
     * Send a message to the sender
     * @param message The messages to send
     */
    default void sendMessage(String... message) {
        for (String s : message) {
            sendMessage(s);
        }
    }

    /**
     * Send a message to the sender
     * @param message The messages to send
     */
    default void sendMessage(List<String> message) {
        for (String s : message) {
            sendMessage(s);
        }
    }

    /**
     * Send a title to the sender
     * @param title    The title text
     * @param subtitle The subtitle text
     */
    default void sendTitle(String title, String subtitle) {
        sendTitle(title, subtitle, 10, 70, 20);
    }

    /**
     * Send a title to the sender
     * @param title    The title text
     * @param subtitle The subtitle text
     * @param fadeIn   The fade int time
     * @param stay     The stay time
     * @param fadeOut  The fade out time
     */
    default void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        sendMessage(title, subtitle);
    }

    /**
     * Send actionbar to the sender
     * @param text The actionbar text
     */
    default void sendActionbar(String text) {
        sendMessage(text);
    }

    /**
     * Send configuration message to the sender
     * @param path The message path
     */
    default void sendPath(String path, Object... args) {
        sendMessage(parse(getPlugin().getConfiguration().getString(path), args));
    }

    /**
     * Send lang message to the sender
     * @param path The message path
     */
    default void sendLang(String path, Object... args) {
        sendMessage(parse(getPlugin().getConfiguration().getMessage(path), args));
    }

    /**
     * Parse the provided text with optional arguments.
     * @param text The text to parse
     * @param args The arguments to insert
     * @return     a parsed text using the current CommandSender.
     */
    default String parse(String text, Object... args) {
        if (text == null) {
            return null;
        }
        return Strings.replaceArgs(text, args);
    }

    /**
     * Parse the provided list of strings
     * @param list The list to parse
     * @param args The arguments to insert
     * @return     a parsed copy of provided list
     */
    default List<String> parse(List<String> list, Object... args) {
        final List<String> finalList = new ArrayList<>();
        for (String s : list) {
            finalList.add(parse(s, args));
        }
        return finalList;
    }
}
