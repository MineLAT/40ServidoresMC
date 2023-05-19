package com.cadiducho.cservidoresmc.bukkit;

import com.cadiducho.cservidoresmc.web.ApiClient;
import com.cadiducho.cservidoresmc.web.Updater;
import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSConsoleSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import com.cadiducho.cservidoresmc.cmd.CSCommandManager;
import com.cadiducho.cservidoresmc.api.CSConfiguration;
import com.cadiducho.cservidoresmc.util.Task;
import com.cadiducho.cservidoresmc.vote.VoteReward;
import com.google.gson.Gson;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Implementación para Bukkit, Spigot y Glowstone
 * @author Cadiducho
 */
public class BukkitPlugin extends JavaPlugin implements CSPlugin {

    // Server instance parameters
    public static final int SERVER_VERSION;
    public static final boolean SPIGOT_SERVER;

    static {
        final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        SERVER_VERSION = Integer.parseInt(version.split("_")[1]);
        boolean spigot = false;
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            spigot = true;
        } catch (ClassNotFoundException ignored) { }
        SPIGOT_SERVER = spigot;
    }

    // Current plugin
    private static BukkitPlugin instance;

    // Parameters
    private CSConfiguration configuration;
    private int logLevel = 3;
    private List<VoteReward> voteRewards;
    private ApiClient apiClient;
    private Updater updater;
    private boolean papiEnabled;
    private CSCommandManager commandManager;

    public static BukkitPlugin get() {
        return instance;
    }

    public BukkitPlugin() {
        Task.setScheduler(new BukkitScheduler());
    }
    
    @Override
    public void onEnable() {
        instance = this;

        // Load config parameters
        saveResource("config.yml", false);
        configuration = new BukkitConfigAdapter(instance, new File(getDataFolder(), "config.yml"));
        logLevel = configuration.getInt("plugin.log-level", 3);
        voteRewards = VoteReward.of(configuration.get("vote.reward"));
        log(3, "Loaded " + voteRewards.size() + " vote reward" + (voteRewards.size() != 1 ? "s" : ""));
        checkDefaultKey();

        // Load client
        apiClient = new ApiClient(instance, new Gson());
        final int seconds = configuration.getInt("vote.client.update");
        if (seconds > 0) {
            Task.asyncTimer(() -> apiClient.updateServerStats(), 0, seconds, TimeUnit.SECONDS);
        }

        // Load updater
        updater = new Updater(instance, PLUGIN_VERSION, getServer().getBukkitVersion().split("-")[0]);
        log(4, "Checkeando nuevas versiones...");
        updater.checkearVersion(null);
        log(3, "Plugin 40ServidoresMC v" + PLUGIN_VERSION + " cargado completamente");

        // Check if PlaceholderAPI is enabled
        papiEnabled = this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (papiEnabled) {
            new PlaceholderHook(this).register();
        }

        // Register commands
        log(4, "Registrando comandos y eventos...");
        registerCommands();

        // Initialize metrics
        Metrics metrics = new Metrics(instance, 3909);
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
                getLogger().severe(msg);
                break;
            case 2:
                getLogger().warning(msg);
                break;
            case 3:
            case 4:
                getLogger().info(msg);
                break;
            case 5:
                Bukkit.getConsoleSender().sendMessage(color(msg));
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            consumer.accept(new BukkitCommandSender(player, this));
        }
    }

    @Override
    public void dispatchCommand(String command) {
        if (Bukkit.isPrimaryThread()) {
            getServer().dispatchCommand(getServer().getConsoleSender(), command);
        } else {
            Task.sync(() -> getServer().dispatchCommand(getServer().getConsoleSender(), command));
        }
    }

    @Override
    public String color(String text) {
        if (text == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', CSPlugin.super.color(text));
    }

    @Override
    public boolean onCommand(CommandSender bukkitSender, Command cmd, String label, String[] args) {
        if (label.startsWith(("40ServidoresMC:").toLowerCase())) {
            label = label.substring(("40ServidoresMC:").length());
        }
        CSCommandSender csCommandSender;
        if (bukkitSender instanceof Player) {
            csCommandSender = new BukkitCommandSender((Player) bukkitSender, instance);
        } else {
            csCommandSender = new CSConsoleSender(instance);
        }

        try {
            commandManager.executeCommand(csCommandSender, label, Arrays.asList(args));
        } catch (Exception ex) {
            log(1, "Error al ejecutar el comando '/" + label + Arrays.toString(args)+"'");
            log(4, ex.getMessage());
            if (ex.getCause() != null) log(4, ex.getCause().getMessage());
        }
        return true;
    }

    public void registerCommands() {
        this.commandManager = new CSCommandManager(instance);
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public class BukkitScheduler implements Task.Scheduler {

        private long ticks(long duration, TimeUnit unit) {
            return unit.toMillis(duration) * 20000 / 1000;
        }

        @Override
        public void sync(Runnable runnable) {
            Bukkit.getScheduler().runTask(BukkitPlugin.this, runnable);
        }

        @Override
        public void syncLater(Runnable runnable, long delay, TimeUnit unit) {
            Bukkit.getScheduler().runTaskLater(BukkitPlugin.this, runnable, ticks(delay, unit));
        }

        @Override
        public void syncTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
            Bukkit.getScheduler().runTaskTimer(BukkitPlugin.this, runnable, ticks(delay, unit), ticks(period, unit));
        }

        @Override
        public void async(Runnable runnable) {
            Bukkit.getScheduler().runTaskAsynchronously(BukkitPlugin.this, runnable);
        }

        @Override
        public void asyncLater(Runnable runnable, long delay, TimeUnit unit) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitPlugin.this, runnable, ticks(delay, unit));
        }

        @Override
        public void asyncTimer(Runnable runnable, long delay, long period, TimeUnit unit) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(BukkitPlugin.this, runnable, ticks(delay, unit), ticks(period, unit));
        }
    }
}
