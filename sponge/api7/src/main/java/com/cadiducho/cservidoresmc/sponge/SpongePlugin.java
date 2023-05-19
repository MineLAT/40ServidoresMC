package com.cadiducho.cservidoresmc.sponge;

import com.cadiducho.cservidoresmc.web.ApiClient;
import com.cadiducho.cservidoresmc.web.Updater;
import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSConsoleSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.cmd.*;
import com.cadiducho.cservidoresmc.api.CSConfiguration;
import com.cadiducho.cservidoresmc.util.Task;
import com.cadiducho.cservidoresmc.vote.VoteReward;
import com.google.gson.Gson;
import com.google.inject.Inject;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Plugin(id = "cservidoresmc", name = "40ServidoresMC", version = CSPlugin.PLUGIN_VERSION)
public class SpongePlugin implements CSPlugin {

    @Inject
    private Logger logger;
    @Inject
    private Game game;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDirectory;
    private final Metrics metrics;

    private CSConfiguration configuration;
    private int logLevel = 3;
    private List<VoteReward> voteRewards;
    private ApiClient apiClient;
    private Updater updater;
    private CSCommandManager commandManager;

    @Inject
    public SpongePlugin(Metrics.Factory metricsFactory) {
        Task.setScheduler(new SpongeScheduler());
        int pluginId = 10604;
        metrics = metricsFactory.make(pluginId);
    }


    @Listener
    public void onServerLoad(GameLoadCompleteEvent event) {
        this.configuration = new SpongeConfigAdapter(this, resolveConfig());
        logLevel = configuration.getInt("plugin.log-level", 3);
        voteRewards = VoteReward.of(configuration.get("vote.reward"));
        log(3, "Loaded " + voteRewards.size() + " vote reward" + (voteRewards.size() != 1 ? "s" : ""));
        checkDefaultKey();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        apiClient = new ApiClient(this, new Gson());
        final int seconds = configuration.getInt("vote.client.update");
        if (seconds > 0) {
            Task.asyncTimer(() -> apiClient.updateServerStats(), 0, seconds, TimeUnit.SECONDS);
        }
        updater = new Updater(this, PLUGIN_VERSION, this.game.getPlatform().getMinecraftVersion().getName());
        updater.checkearVersion(new CSConsoleSender(this));

        registerCommands();
    }

    @Override
    public void onReload() {
        configuration.reload();
        logLevel = configuration.getInt("plugin.log-level", 3);
        voteRewards = VoteReward.of(configuration.get("vote.reward"));
        log(3, "Loaded " + voteRewards.size() + " vote reward" + (voteRewards.size() != 1 ? "s" : ""));
        checkDefaultKey();
    }

    @Override
    public void logMessage(int level, String msg) {
        switch (level) {
            case 1:
                logger.error(msg);
                break;
            case 2:
                logger.warn(msg);
                break;
            case 3:
                logger.info(msg);
                break;
            case 4:
                logger.debug(msg);
                break;
            case 5:
                Sponge.getServer().getConsole().sendMessage(colorText(msg));
                break;
            default:
                break;
        }
    }

    @Override
    public CSConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public List<VoteReward> getVoteRewards() {
        return voteRewards;
    }

    @Override
    public ApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public void forEachOnlinePlayer(Consumer<CSCommandSender> consumer) {
        for (Player player : game.getServer().getOnlinePlayers()) {
            consumer.accept(new SpongeCommandSender(player, this));
        }
    }

    @Override
    public void dispatchCommand(String command) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
    }

    public Text colorText(String text) {
        return TextSerializers.FORMATTING_CODE.deserialize(color(text));
    }

    private Path resolveConfig() {
        Path path = this.configDirectory.resolve("config.yml");
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(this.configDirectory);
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    Files.copy(is, path);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return path;
    }

    public void registerCommands() {
        this.commandManager = new CSCommandManager(this);
        CommandManager cmdService = Sponge.getCommandManager();
        this.commandManager.getCommands().forEach(cmd -> {
            List<String> alias = new ArrayList<>(cmd.getAliases());
            alias.add(cmd.getName());
            cmdService.register(this, new SpongeCommandExecutor(commandManager, cmd), alias);
        });
    }

    public class SpongeScheduler implements Task.Scheduler {

        private org.spongepowered.api.scheduler.Task.Builder builder() {
            return org.spongepowered.api.scheduler.Task.builder();
        }

        @Override
        public void sync(Runnable runnable) {
            builder().execute(runnable).submit(SpongePlugin.this);
        }

        @Override
        public void syncLater(Runnable runnable, long delay, TimeUnit unit) {
            builder().execute(runnable).delay(delay, unit).submit(SpongePlugin.this);
        }

        @Override
        public void syncTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
            builder().execute(runnable).delay(delay, unit).interval(period, unit).submit(SpongePlugin.this);
        }

        @Override
        public void async(Runnable runnable) {
            builder().execute(runnable).async().submit(SpongePlugin.this);
        }

        @Override
        public void asyncLater(Runnable runnable, long delay, TimeUnit unit) {
            builder().execute(runnable).delay(delay, unit).async().submit(SpongePlugin.this);
        }

        @Override
        public void asyncTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
            builder().execute(runnable).delay(delay, unit).interval(period, unit).async().submit(SpongePlugin.this);
        }
    }
}