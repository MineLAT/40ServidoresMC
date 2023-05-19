package com.cadiducho.cservidoresmc.api;

import com.cadiducho.cservidoresmc.web.ApiClient;
import com.cadiducho.cservidoresmc.web.Updater;
import com.cadiducho.cservidoresmc.util.Strings;
import com.cadiducho.cservidoresmc.vote.VoteReward;

import java.util.List;
import java.util.function.Consumer;

public interface CSPlugin {

    String PLUGIN_VERSION = "4.0";
    int CONFIG_VERSION = 3;

    /**
     * Method to reload the plugin
     */
    void onReload();

    /**
     * Log message to console after check if level is applicable
     * @param level The log level
     * @param msg   The message to log
     */
    default void log(int level, String msg) {
        if (getLogLevel() >= level) {
            logMessage(level, msg);
        }
    }

    /**
     * Log message to console
     * @param level The log level
     * @param msg   The message to log
     */
    void logMessage(int level, String msg);

    /**
     * Datos de configuración del plugin, con implementación para cada tipo de servidor
     * @return config
     */
    CSConfiguration getConfiguration();

    /**
     * Get current log level
     * @return a log level
     */
    default int getLogLevel() {
        return 3;
    }

    /**
     * Get vote rewards
     * @return a list with vote rewards
     */
    List<VoteReward> getVoteRewards();

    /**
     * Instancia del cliente HTTP para la API
     * @return API client
     */
    ApiClient getApiClient();

    /**
     * Instancia del actualizador
     * @return updater
     */
    Updater getUpdater();

    /**
     * Perform the provided action to each online player adapted to command sender
     * @param consumer the action to run for every player.
     */
    void forEachOnlinePlayer(Consumer<CSCommandSender> consumer);

    /**
     * Ejecutar un comando deseado por la consola del servidor
     * @param command El comando deseado
     */
    void dispatchCommand(String command);

    /**
     * Colorize the provided text
     * @param text The text to color
     * @return     a colored text
     */
    default String color(String text) {
        return Strings.rgb(text);
    }

    /**
     * Comprobar si la configuración tiene una clave válida
     */
    default void checkDefaultKey() {
        if (getConfiguration().getInt("configVer", 0) != CONFIG_VERSION) {
            log(1, "¡Tu configuración es de una versión más antigua a la de este plugin!");
            log(1, "Actualiza la configuración para evitar errores.");
        }
        if (getConfiguration().getString("vote.client.key", "key").equalsIgnoreCase("key")) {
            log(1, "¡Atención! La clave del servidor no está correctamente configurada");
            log(1, "Accede a la configuración y modifica 'clave' con el valor correcto obtenido en la página web.");
            log(1, "Este error hará que el plugin no funcione correctamente.");
        }
    }
}
