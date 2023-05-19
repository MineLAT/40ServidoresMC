package com.cadiducho.cservidoresmc.cmd;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.api.CSPlugin;
import lombok.Getter;

import java.util.*;

public class CSCommandManager {

    @Getter private final CSPlugin plugin;
    private final Map<String, CSCommand> commands;
    private final List<CSCommand> commandList;

    public CSCommandManager(CSPlugin plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.commandList = new ArrayList<>();

        registerCommand(new ReloadCMD());
        registerCommand(new StatsCMD());
        registerCommand(new TestCMD());
        registerCommand(new UpdateCMD());
        registerCommand(new VoteCMD());
    }

    public List<CSCommand> getCommands() {
        return this.commandList;
    }

    /**
     * Registrar un nuevo comando, probablemente específico por cada plataforma, a este manager
     * @param command El comando a registrar
     */
    public void registerCommand(CSCommand command) {
        this.commandList.add(command);
        this.commands.put(command.getName(), command);
        command.getAliases().forEach(alias -> this.commands.put(alias, command));
    }

    /**
     * Ejecutar un comando. Buscarlo en el Mapa de comandos y alias y si es posible, invocarlo
     * @param sender Quien ejecuta el comando
     * @param label El comando escrito
     * @param args Los argumentos del comando
     */
    public void executeCommand(final CSCommandSender sender, String label, List<String> args) {
        Optional<CSCommand> command = Optional.ofNullable(commands.getOrDefault(label, null));
        if (command.isPresent()) {
            CSCommand cmd = command.get();
            if (!cmd.isAuthorized(sender)) {
                sender.sendLang("command.result.not-perm", cmd.getPermission());
                return;
            }
            CSCommand.CommandResult result = cmd.execute(plugin, sender, label, args);
            switch (result) {
                case COOLDOWN:
                    sender.sendLang("command.result.cooldown", cmd.getPermission());
                    break;
                case NO_PERMISSION:
                    sender.sendLang("command.result.not-perm", cmd.getPermission());
                    break;
                case ERROR:
                    sender.sendLang("command.result.error", cmd.getPermission());
                    break;
                case ONLY_PLAYER:
                    sender.sendLang("command.result.only-player", cmd.getPermission());
                    break;
            }
        }
    }
}
