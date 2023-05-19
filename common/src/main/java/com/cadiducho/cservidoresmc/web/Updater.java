package com.cadiducho.cservidoresmc.web;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSConsoleSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.web.model.updater.UpdaterInfo;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Clase para comprobar las actualizaciones a través de Github
 * @author Cadiducho
 */
public class Updater {

    private static String versionInstalada, versionMinecraft;
    private static CSPlugin plugin;

    public Updater(CSPlugin instance, String vInstalada, String vMinecraft) {
        plugin = instance;
        versionInstalada = vInstalada;
        versionMinecraft = vMinecraft;
    }

    /**
     * Comprobar si hay nueva versión
     * @param sender Jugador al que se avisará
     */
    public void checkearVersion(CSCommandSender sender) {
        checkearVersion(sender, false);
    }

    /**
     * Comprobar si hay nueva versión
     * @param sender Jugador al que se avisará
     * @param confirmation Si es true, se avisará si no hay nueva versión
     */
    public void checkearVersion(CSCommandSender sender, boolean confirmation) {
        if (sender == null) {
            // Se hace al iniciar el plugin
            sender = new CSConsoleSender(plugin);
        }
        plugin.log(4, "Buscando nueva versión para Minecraft " + versionMinecraft + "...");

        final CSCommandSender finalSender = sender;
        fetchUpdate().thenAccept((UpdaterInfo updaterInfo) -> {
            Optional<Map.Entry<String, String>> recommendedVersion = updaterInfo.getPluginForMinecraft(versionMinecraft);
            if (recommendedVersion.isPresent()) {
                String updaterVersion = recommendedVersion.get().getKey();
                String updateDescription = recommendedVersion.get().getValue();

                // Si existe versión recomendada para esa versión de minecraft, pero no es la que está instalada, avisar
                if (!updaterVersion.equals(versionInstalada)) {
                    String link = String.format("https://github.com/Cadiducho/40ServidoresMC/releases/tag/v%s", updaterVersion);
                    finalSender.sendLang("updater.new-version", updaterVersion, updateDescription, link);
                } else {
                    finalSender.sendLang("updater.updated");
                }
            } else if (confirmation) {
                finalSender.sendLang("updater.any-version");
            }
        }).exceptionally(e -> {
            plugin.log(1, plugin.getConfiguration().getString("messages.updater.error"));
            plugin.log(4, "Causa: " + e.getMessage());
            return null;
        });
    }

    private CompletableFuture<UpdaterInfo> fetchUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/Cadiducho/40ServidoresMC/development/etc/v3.json"); //ToDo: cambiar el branch
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (Reader reader = new InputStreamReader(connection.getInputStream())) {
                    return new Gson().fromJson(reader, UpdaterInfo.class);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot execute Updater fetch", e);
            }
        });

    }

}
