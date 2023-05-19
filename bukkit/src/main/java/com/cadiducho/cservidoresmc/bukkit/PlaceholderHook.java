package com.cadiducho.cservidoresmc.bukkit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

@RequiredArgsConstructor
public class PlaceholderHook extends PlaceholderExpansion {

    private final BukkitPlugin plugin;

    @Override
    public @NonNull String getIdentifier() {
        return "40servidoresmc";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Rubenicos";
    }

    @Override
    public @NonNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params) {
        final String[] args = params.split("_", 2);
        switch (args[0].toLowerCase()) {
            case "server":
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("name")) {
                        return String.valueOf(plugin.getApiClient().getLastStats().getServerName());
                    } else if (args[1].equalsIgnoreCase("position")) {
                        return String.valueOf(plugin.getApiClient().getLastStats().getPosition());
                    }
                }
                break;
            case "votes":
                if (args.length > 1) {
                    switch (args[1].toLowerCase()) {
                        case "day":
                            return String.valueOf(plugin.getApiClient().getLastStats().getDayVotes());
                        case "day_rewarded":
                            return String.valueOf(plugin.getApiClient().getLastStats().getRewardedDayVotes());
                        case "week":
                            return String.valueOf(plugin.getApiClient().getLastStats().getWeekVotes());
                        case "week_rewarded":
                            return String.valueOf(plugin.getApiClient().getLastStats().getRewardedWeekVotes());
                        default:
                            break;
                    }
                }
                break;
            case "vote":
                if (args.length > 1) {
                    switch (args[1].toLowerCase()) {
                        case "type":
                            return plugin.getApiClient().getSavedResponse(player.getName()).getVoteType();
                        case "web":
                            return plugin.getApiClient().getSavedResponse(player.getName()).getWeb();
                        case "status":
                            return plugin.getApiClient().getSavedResponse(player.getName()).getStatus().name();
                        case "msg":
                        case "message":
                            return plugin.getApiClient().getSavedResponse(player.getName()).getMsg();
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
        return params + " is not a valid request";
    }
}
