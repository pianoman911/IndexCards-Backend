package de.pianoman911.indexcards.logic;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.sql.DatabaseType;
import de.pianoman911.indexcards.sql.SqlEscape;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class IndexCardsLogic {

    private final IndexCards service;
    private final Cache<UUID, User> sessions = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

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

    public CompletableFuture<IndexCard> nextCard(User user, String g) {
        CompletableFuture<IndexCard> future = new CompletableFuture<>();
        String statement = "SELECT * FROM CARD.cards WHERE id NOT IN (SELECT card FROM USER.progress WHERE user = " + SqlEscape.word(user.name()) + " AND time > UNIX_TIMESTAMP())" + (g == null ? ";" : " AND cards.group LIKE " + SqlEscape.word(g) + "%;");
        service.queue().readAsync(DatabaseType.BASIC, statement).thenAccept(resultSet -> {
            try {
                List<String> answers = new ArrayList<>();
                String question;
                int id;
                String group;
                if (resultSet.next()) {
                    question = resultSet.getString("question");
                    answers.add(resultSet.getString("answer"));
                    id = resultSet.getInt("id");
                    group = resultSet.getString("group");
                    service.logic().refreshSession(user);
                    future.complete(new IndexCard(id, question, answers, group));
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
        String select = "SELECT * FROM users WHERE name = " + SqlEscape.word(name) + "LIMIT 1";
        String statement = "INSERT INTO USER.users (name, password) VALUE (" + SqlEscape.word(name) + ", '" + password + "')";
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
        String statement = "INSERT INTO USER.progress (`user`, `card`, `time`, `status`) VALUE (" + SqlEscape.word(user.name()) + ", '" + id + "', '" + timestamp + "', '" + status + "') ON DUPLICATE KEY UPDATE time = '" + timestamp + "', status = '" + status + "'";
        service.queue().write(DatabaseType.USER, statement);
    }

    public CompletableFuture<IndexCard> card(int id) {
        CompletableFuture<IndexCard> future = new CompletableFuture<>();
        String statement = "SELECT * FROM CARD.cards WHERE id = " + id + " LIMIT 1";
        service.queue().readAsync(DatabaseType.BASIC, statement).thenAccept(resultSet -> {
            try {
                List<String> answers = new ArrayList<>();
                String question = null;
                String group = null;
                if (resultSet.next()) {
                    question = resultSet.getString("question");
                    answers.add(resultSet.getString("answer"));
                    group = resultSet.getString("group");
                }
                future.complete(new IndexCard(id, question, answers, group));
            } catch (SQLException ignored) {

            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Integer> nextStatus(User user, IndexCard card) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String statement = "SELECT * FROM USER.progress WHERE user = " + SqlEscape.word(user.name()) + " AND card = " + card.id() + " LIMIT 1";
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
        String statement = "SELECT DISTINCT `group` FROM CARD.cards;";
        service.queue().readAsync(DatabaseType.CARD, statement).thenAccept(resultSet -> {
            Set<String> groups = new HashSet<>();
            try {
                while (resultSet.next()) {
                    String group = resultSet.getString("group");
                    groups.addAll(Arrays.asList(group.split("/")));
                }
                future.complete(groups);
            } catch (SQLException ignored) {
                future.complete(Collections.emptySet());
            }
        });
        return future;
    }
}
