package de.pianoman911.indexcards.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SqlEscape {

    private SqlEscape() {
    }

    public static @NotNull String escape(@Nullable String string) {
        StringBuilder builder = new StringBuilder().append('\'');
        if (string != null) {
            for (char character : string.toCharArray()) {
                if (character == '\'' || character == '\\' || character == '"' || character == '\0') {
                    builder.append('\\');
                }
                builder.append(character);
            }
        }
        return builder.append('\'').toString();
    }

    public static @NotNull String word(@Nullable String username) {
        return word(username, true);
    }

    public static @NotNull String word(@Nullable String username, boolean escape) {
        return word(username, 16, escape);
    }

    public static @NotNull String word(@Nullable String username, int limit, boolean escape) {
        StringBuilder builder = new StringBuilder();
        if (escape) {
            builder.append('\'');
        }

        if (username != null) {
            char[] chars = username.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (i >= limit) {
                    break;
                }
                char c = chars[i];

                // only allow 0-9, A-Z, _ & a-z /
                if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '/' || (c >= 'a' && c <= 'z')) {
                    builder.append(c);
                }
            }
        }

        if (escape) {
            builder.append('\'');
        }
        return builder.toString();
    }
}