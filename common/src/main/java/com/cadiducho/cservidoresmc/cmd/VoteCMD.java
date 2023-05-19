package com.cadiducho.cservidoresmc.cmd;

import com.cadiducho.cservidoresmc.util.Cooldown;
import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.vote.VoteReward;
import com.cadiducho.cservidoresmc.web.model.VoteResponse;
import com.cadiducho.cservidoresmc.web.model.VoteStatus;

import java.util.Arrays;
import java.util.List;

/**
 * Comando para validar el voto en 40ServidoresMC
 */
public class VoteCMD extends CSCommand {

    protected VoteCMD() {
        super("voto40", "40servidores.voto", Arrays.asList("votar40", "vote40", "mivoto40"),
                "Valida tu voto en el servidor",
                "Usa /voto40 àra validar tu voto en el servidor");
    }

    final Cooldown cooldown = new Cooldown(60);

    @Override
    public CommandResult execute(CSPlugin plugin, CSCommandSender sender, String label, List<String> args) {
        if (sender.isConsole()) {
            return CommandResult.ONLY_PLAYER;
        }

        if (cooldown.isCoolingDown(sender.getName())) {
            return CommandResult.COOLDOWN;
        }

        cooldown.setOnCooldown(sender.getName());

        sender.sendLang("command.vote.looking");
        plugin.getApiClient().validateVote(sender.getName()).thenAccept((VoteResponse voteResponse) -> {
            String web = voteResponse.getWeb();
            VoteStatus status = voteResponse.getStatus();

            switch (status) {
                case NOT_VOTED:
                    sender.sendLang("command.vote.not-voted", web);
                    break;
                case SUCCESS:
                    for (VoteReward reward : plugin.getVoteRewards()) {
                        reward.giveTo(sender, sender.getName(), sender.getUniqueId());
                    }
                    break;
                case ALREADY_VOTED:
                    sender.sendLang("command.vote.already-voted");
                    break;
                case INVALID_kEY:
                    sender.sendLang("command.result.bad-password");
                    break;
                default:
                    sender.sendLang("command.vote.error");
                    break;
            }
        }).exceptionally(e -> {
            sender.sendLang("command.result.exception");
            plugin.log(1, "Excepción intentando votar: " + e.getMessage());
            return null;
        });

        return CommandResult.SUCCESS;
    }
}
