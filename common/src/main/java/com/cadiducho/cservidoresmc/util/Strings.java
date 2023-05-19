package com.cadiducho.cservidoresmc.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Strings {

    public static String single(Object object) {
        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof Iterable) {
            for (Object o : (Iterable<?>) object) {
                return String.valueOf(o);
            }
        } else if (object instanceof Object[]) {
            if (((Object[]) object).length > 0) {
                return String.valueOf(((Object[]) object)[0]);
            }
        } else if (object.getClass().isArray()) {
            if (Array.getLength(object) > 0) {
                return String.valueOf(Array.get(object, 0));
            }
        } else {
            return String.valueOf(object);
        }
        return "<empty>";
    }

    public static List<String> list(Object object) {
        final List<String> list = new ArrayList<>();
        if (object == null) {
            return list;
        }
        if (object instanceof Iterable) {
            for (Object o : (Iterable<?>) object) {
                list.add(String.valueOf(o));
            }
        } else if (object instanceof Object[]) {
            for (int i = 0; i < ((Object[]) object).length; i++) {
                list.add(String.valueOf(((Object[]) object)[i]));
            }
        } else {
            list.add(String.valueOf(object));
        }
        return list;
    }

    public static String replaceArgs(String s, Object... args) {
        // Taken from TabooLib
        if (args.length < 1 || s.isEmpty()) {
            return s;
        }
        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());
        for (int i = 0; i < chars.length; i++) {
            final int mark = i;
            if (chars[i] == '{') {
                int num = 0;
                while (i + 1 < chars.length) {
                    if (Character.isDigit(chars[i + 1])) {
                        i++;
                        num *= 10;
                        num += chars[i] - '0';
                        continue;
                    }
                    break;
                }
                if (i != mark && i + 1 < chars.length && chars[i + 1] == '}') {
                    i++;
                    if (num < args.length) { // Avoid IndexOutOfBoundsException
                        builder.append(args[num]);
                    } else {
                        builder.append('{').append(num).append('}');
                    }
                } else {
                    i = mark;
                }
            }
            if (mark == i) {
                builder.append(chars[i]);
            }
        }
        return builder.toString();
    }

    public static String rgb(String s) {
        if (!s.contains("&#")) {
            return s;
        }

        StringBuilder builder = new StringBuilder();

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i + 7 < chars.length && chars[i] == '&' && chars[i + 1] == '#') {
                StringBuilder color = new StringBuilder();
                for (int c = i + 2; c < chars.length && color.length() < 6; c++) {
                    color.append(chars[c]);
                }
                if (color.length() == 6) {
                    builder.append(hex(color.toString()));
                    i += color.length() + 1;
                } else {
                    builder.append(chars[i]);
                }
            } else {
                builder.append(chars[i]);
            }
        }

        return builder.toString();
    }

    public static String hex(String hex) {
        try {
            Integer.parseInt(hex, 16);
        } catch (NumberFormatException ex) {
            return "&#" + hex;
        }

        StringBuilder builder = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            builder.append("§").append(c);
        }

        return builder.toString();
    }
}
