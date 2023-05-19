package com.cadiducho.cservidoresmc.vote;

import com.cadiducho.cservidoresmc.api.CSCommandSender;
import com.cadiducho.cservidoresmc.util.Strings;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class VoteReward {

    public static List<VoteReward> of(Object object) {
        final List<VoteReward> list = new ArrayList<>();
        if (object == null) {
            return list;
        }

        if (object instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                final VoteReward reward = of(String.valueOf(entry.getKey()), entry.getValue());
                if (reward != null) {
                    list.add(reward);
                }
            }
        } else if (object instanceof Iterable) {
            for (Object o : (Iterable<?>) object) {
                list.addAll(of(o));
            }
        } else {
            final String[] split = String.valueOf(object).split(":", 2);
            if (split.length > 1) {
                final VoteReward reward = of(split[0], split[1].trim());
                if (reward != null) {
                    list.add(reward);
                }
            }
        }
        return list;
    }

    public static VoteReward of(String key, Object value) {
        switch (key.trim().toLowerCase()) {
            case "actionbar":
                return new Actionbar(Strings.single(value));
            case "broadcast":
                return new Broadcast(Strings.list(value));
            case "command":
            case "cmd":
                return new Command(Strings.single(value));
            case "message":
            case "msg":
                return new Message(Strings.list(value));
            case "title":
                if (value instanceof Map) {
                    final String title = Strings.single(((Map<?, ?>) value).get("title"));
                    final String subtitle = Strings.single(((Map<?, ?>) value).get("subtitle"));
                    final int fadeIn = integer(((Map<?, ?>) value).get("fadeIn"), 10);
                    final int stay = integer(((Map<?, ?>) value).get("stay"), 70);
                    final int fadeOut = integer(((Map<?, ?>) value).get("fadeOut"), 20);
                    return new Title(title, subtitle, fadeIn, stay, fadeOut);
                } else {
                    final List<String> list = Strings.list(value);
                    return new Title(list.size() > 0 ? list.get(0) : null, list.size() > 1 ? list.get(1) : null, 10, 70, 20);
                }
            default:
                return null;
        }
    }

    private static int integer(Object object, int def) {
        if (object instanceof Integer) {
            return (int) object;
        }
        try {
            return Integer.parseInt(String.valueOf(object));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public abstract void giveTo(CSCommandSender sender, Object... args);

    @RequiredArgsConstructor
    public static class Actionbar extends VoteReward {
        private final String text;

        @Override
        public void giveTo(CSCommandSender sender, Object... args) {
            sender.sendActionbar(sender.parse(text, args));
        }
    }

    @RequiredArgsConstructor
    public static class Broadcast extends VoteReward {
        private final List<String> list;

        @Override
        public void giveTo(CSCommandSender sender, Object... args) {
            final List<String> message = sender.parse(list, args);
            sender.getPlugin().forEachOnlinePlayer(player -> player.sendMessage(message));
        }
    }

    @RequiredArgsConstructor
    public static class Command extends VoteReward {
        private final String cmd;

        @Override
        public void giveTo(CSCommandSender sender, Object... args) {
            sender.getPlugin().dispatchCommand(sender.parse(cmd, args));
        }
    }

    @RequiredArgsConstructor
    public static class Message extends VoteReward {
        private final List<String> msg;

        @Override
        public void giveTo(CSCommandSender sender, Object... args) {
            sender.sendMessage(sender.parse(msg, args));
        }
    }

    @RequiredArgsConstructor
    public static class Title extends VoteReward {
        private final String title;
        private final String subtitle;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        @Override
        public void giveTo(CSCommandSender sender, Object... args) {
            sender.sendTitle(sender.parse(title, args), sender.parse(subtitle, args), fadeIn, stay, fadeOut);
        }
    }
}
