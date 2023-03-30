package de.pianoman911.indexcards.logic;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.sql.DatabaseType;
import de.pianoman911.indexcards.sql.SqlEscape;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexCardsLogic {

    private static final Pattern LINK_PATTERN = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-äöüß]*[\\w@?^=%&\\/~+#-])");

    private final IndexCards service;
    private final Cache<UUID, User> sessions = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private final Map<String, String> images = new HashMap<>();

    public IndexCardsLogic(IndexCards service) {
        this.service = service;
    }

    public CompletableFuture<User> handleAuth(String name, String password) {
        CompletableFuture<User> future = new CompletableFuture<>();
        String statement = "SELECT * FROM users WHERE name = " + SqlEscape.word(name) + " AND password = '" + password + "' LIMIT 1";
        service.queue().readAsync(DatabaseType.USER, statement).thenAccept(resultSet -> {
            try {
                if (resultSet.next()) {
                    User user = new User(name, password);
                    service.logic().refreshSession(user);
                    future.complete(user);
                    return;
                }
            } catch (SQLException ignored) {

            }
            future.complete(null);
        });
        return future;
    }

    public User session(String session) {
        return sessions.getIfPresent(UUID.fromString(session));
    }

    public User refreshSession(User user) {
        sessions.asMap().entrySet().removeIf(entry -> entry.getValue().equals(user));
        sessions.put(user.session(), user);
        return user;
    }

    public IndexCards service() {
        return service;
    }

    public Cache<UUID, User> sessions() {
        return sessions;
    }

    public Map<String, String> images() {
        return images;
    }

    public CompletableFuture<IndexCard> nextCard(User user, String g) {
        CompletableFuture<IndexCard> future = new CompletableFuture<>();
        if (g != null) {
            g = SqlEscape.escape(g);
            g = g.substring(1, g.length() - 1);
            g = "'" + g + "%'";
        }
        String statement = "SELECT * FROM " + DatabaseType.CARD.credentials().database() + ".cards WHERE id NOT IN (SELECT card FROM " + DatabaseType.USER.credentials().database() + ".progress WHERE user = " + SqlEscape.word(user.name()) + " AND time > UNIX_TIMESTAMP())" + (g == null ? ";" : " AND cards.group LIKE " + g + ";");
        service.queue().readAsync(DatabaseType.BASIC, statement).thenAccept(resultSet -> {
            try {
                List<String> answers = new ArrayList<>();
                String question;
                int id;
                String group;
                if (resultSet.next()) {
                    question = resultSet.getString("question");
                    id = resultSet.getInt("id");
                    group = resultSet.getString("group");
                    service.logic().refreshSession(user);
                    String a = "SELECT * FROM " + DatabaseType.CARD.credentials().database() + ".answers WHERE card = " + id + ";";
                    ResultSet ar = service.queue().read(DatabaseType.CARD, a);
                    while (ar.next()) {
                        answers.add(findLinkAndTryObfuscate(ar.getString("answer")));
                    }
                    future.complete(new IndexCard(id, findLinkAndTryObfuscate(question), answers, group));
                } else {
                    future.complete(null);
                }
            } catch (SQLException ignored) {
                future.complete(null);
            }
            future.complete(null);
        });

        return future;
    }

    public CompletableFuture<User> createUser(String name, String password) {
        CompletableFuture<User> future = new CompletableFuture<>();
        String escapedName = SqlEscape.word(name);
        if (!escapedName.equals("'" + name + "'")) {
            future.complete(null);
            return future;
        }
        String select = "SELECT * FROM users WHERE name = " + escapedName + "LIMIT 1";
        String statement = "INSERT INTO " + DatabaseType.USER.credentials().database() + ".users (name, password) VALUE (" + escapedName + ", '" + password + "')";
        service.queue().readAsync(DatabaseType.USER, select).thenAccept(resultSet -> {
            try {
                if (!resultSet.next()) {
                    service.queue().write(DatabaseType.USER, statement);
                    User user = new User(name, password);
                    future.complete(user);
                    return;
                }
            } catch (SQLException ignored) {

            }
            future.complete(null);
        });
        return future;
    }

    public void doneCard(User user, int id, long timestamp, int status) {
        String statement = "INSERT INTO " + DatabaseType.USER.credentials().database() + ".progress (`user`, `card`, `time`, `status`) VALUE (" + SqlEscape.word(user.name()) + ", '" + id + "', '" + timestamp + "', '" + status + "') ON DUPLICATE KEY UPDATE time = '" + timestamp + "', status = '" + status + "'";
        service.queue().write(DatabaseType.USER, statement);
    }

    public CompletableFuture<IndexCard> card(int id) {
        CompletableFuture<IndexCard> future = new CompletableFuture<>();
        String statement = "SELECT * FROM " + DatabaseType.CARD.credentials().database() + ".cards WHERE id = " + id + " LIMIT 1";
        service.queue().readAsync(DatabaseType.BASIC, statement).thenAccept(resultSet -> {
            try {
                List<String> answers = new ArrayList<>();
                String question = null;
                String group = null;
                if (resultSet.next()) {
                    question = resultSet.getString("question");
                    group = resultSet.getString("group");
                }
                String a = "SELECT * FROM " + DatabaseType.CARD.credentials().database() + ".answers WHERE card = " + id + ";";
                ResultSet ar = service.queue().read(DatabaseType.CARD, a);
                while (ar.next()) {
                    answers.add(findLinkAndTryObfuscate(ar.getString("answer")));
                }
                future.complete(new IndexCard(id, findLinkAndTryObfuscate(question), answers, group));
            } catch (SQLException ignored) {

            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Integer> nextStatus(User user, IndexCard card) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String statement = "SELECT * FROM " + DatabaseType.USER.credentials().database() + ".progress WHERE user = " + SqlEscape.word(user.name()) + " AND card = " + card.id() + " LIMIT 1";
        service.queue().readAsync(DatabaseType.USER, statement).thenAccept(resultSet -> {
            try {
                if (resultSet.next()) {
                    future.complete(Math.min(4, resultSet.getInt("status")) + 1);
                } else {
                    future.complete(1);
                }
                return;
            } catch (SQLException ignored) {

            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Set<String>> groups() {
        CompletableFuture<Set<String>> future = new CompletableFuture<>();
        String statement = "SELECT DISTINCT `group` FROM " + DatabaseType.CARD.credentials().database() + ".cards;";
        service.queue().readAsync(DatabaseType.CARD, statement).thenAccept(resultSet -> {
            Set<String> groups = new HashSet<>();
            try {
                while (resultSet.next()) {
                    String group = resultSet.getString("group");
                    String[] splitted = group.split("/");
                    String root = "";
                    for (int i = 0; i < splitted.length; i++) {
                        root += splitted[i];
                        groups.add(root);
                        if (splitted.length - 1 > i) {
                            root += "-";
                        }
                    }
                }
                future.complete(groups);
            } catch (SQLException ignored) {
                future.complete(Collections.emptySet());
            }
        });
        return future;
    }

    private String findLinkAndTryObfuscate(String text) {
        Matcher matcher = LINK_PATTERN.matcher(text);
        if (matcher.find()) {
            String link = matcher.group();
            String obfuscated = "https://api-indexcards.finndohrmann.de/api/image/" + obfuscateLink(link);
            return matcher.replaceAll(obfuscated);
        }
        return text;
    }

    private String obfuscateLink(String link) {
        if (link == null) {
            return null;
        }
        String hashed = Hashing.sha256().hashString(link, StandardCharsets.UTF_8).toString();
        images.put(hashed, link);
        return hashed;
    }
}
