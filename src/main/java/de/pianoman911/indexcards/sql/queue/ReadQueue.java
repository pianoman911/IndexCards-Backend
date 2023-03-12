package de.pianoman911.indexcards.sql.queue;

import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.sql.DatabaseType;
import de.pianoman911.indexcards.sql.SimpleStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public record ReadQueue(IndexCards service) {

    private static final Logger LOGGER = LogManager.getLogger(ReadQueue.class);

    private @NotNull CompletableFuture<ResultSet> queueStatement(@NotNull SimpleStatement statement) {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet result;
            try (Connection connection = service.sql().connection(statement.database())) {
                try (Statement sqlStatement = connection.createStatement()) {
                    result = sqlStatement.executeQuery(statement.statement());
                }
            } catch (SQLException exception) {
                LOGGER.error(
                        "Statement id {} \"{}\" on database {} with action {} caused {}",
                        statement.id(), statement.statement(), statement.database(), statement.action(), exception
                );
                throw new RuntimeException(exception);
            }
            return result;
        }, service.queue().readPool());
    }

    public @NotNull CompletableFuture<ResultSet> queueStatementAsync(@NotNull DatabaseType database, @NotNull String statement) {
        return queueStatement(new SimpleStatement(SimpleStatement.Action.READ, database, statement));
    }

    public @NotNull ResultSet queueStatement(@NotNull DatabaseType database, @NotNull String statement) {
        return queueStatement(new SimpleStatement(SimpleStatement.Action.READ, database, statement)).join();
    }
}
