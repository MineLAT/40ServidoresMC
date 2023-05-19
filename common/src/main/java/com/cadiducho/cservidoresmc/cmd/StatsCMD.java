package com.cadiducho.cservidoresmc.cmd;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.web.model.ServerStats;
import com.cadiducho.cservidoresmc.web.model.ServerVote;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Comando para obtener las estadísticas de tu servidor en 40ServidoresMC
 * @author Cadiducho
 */
public class StatsCMD extends CSCommand {

    protected StatsCMD() {
        super("stats40", "40servidores.stats", Collections.emptyList(),
                "Comprueba las estadísticas de voto",
                "Usa /stats40 para obtener las estadísticas de voto");
    }

    @Override
    public CommandResult execute(CSPlugin plugin, CSCommandSender sender, String label, List<String> args) {
        plugin.getApiClient().fetchServerStats().thenAccept((ServerStats serverStats) -> {
            if (serverStats.getServerName() == null) { //clave mal configurada
                sender.sendLang("command.result.bad-password");
                return;
            }

            sender.sendLang("command.stats.info",
                    serverStats.getServerName(),
                    serverStats.getPosition(),
                    serverStats.getDayVotes(),
                    serverStats.getRewardedDayVotes(),
                    serverStats.getWeekVotes(),
                    serverStats.getRewardedWeekVotes()
                    );

            if (serverStats.getLastVotes() != null) {
                StringJoiner joiner = new StringJoiner("&6, ");
                for (ServerVote vote : serverStats.getLastVotes()) {
                    String color = vote.isRewarded() ? "&a" : "&c";
                    joiner.add(color).add(vote.getName());
                }
                sender.sendLang("command.stats.last-votes", joiner.toString());
            }
        }).exceptionally(ex -> {
            sender.sendLang("command.result.exception");
            plugin.log(1, "Excepción obteniendo estadisticas: " + ex.getMessage());
            return null;
        });
        return CommandResult.SUCCESS;
    }
}
