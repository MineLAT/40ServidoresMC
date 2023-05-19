package com.cadiducho.cservidoresmc.cmd;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.util.Task;
import com.cadiducho.cservidoresmc.vote.VoteReward;

import java.util.Collections;
import java.util.List;

/**
 * Comando para probar las recompensas sin votar realmente
 * @author Cadiducho
 */
public class TestCMD extends CSCommand {

    protected TestCMD() {
        super("test40", "40servidores.test", Collections.emptyList(),
                "Realiza una prueba de premios al votar",
                "Usa /test40 para probar tus premios al votar");
    }

    @Override
    public CommandResult execute(CSPlugin plugin, CSCommandSender sender, String label, List<String> args) {
        if (sender.isConsole()) {
            return CommandResult.ONLY_PLAYER;
        }

        Task.async(() -> {
            sender.sendLang("command.test");
            for (VoteReward reward : plugin.getVoteRewards()) {
                reward.giveTo(sender, sender.getName(), sender.getUniqueId());
            }
        });

        return CommandResult.SUCCESS;
    }
}
