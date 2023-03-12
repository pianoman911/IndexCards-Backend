package de.pianoman911.indexcards.sql.queue;
// Created by booky10 in TJCProxy (22:36 02.06.21)

import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.sql.DatabaseType;
import de.pianoman911.indexcards.sql.SimpleStatement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public record WriteQueue(IndexCards service) {

    private static final Logger LOGGER = LogManager.getLogger(WriteQueue.class);

    public void queueStatement(@NotNull SimpleStatement statement) {
        service.queue().writePool().execute(() -> {
            try (Connection connection = service.sql().connection(statement.database())) {
                try (Statement sqlStatement = connection.createStatement()) {
                    sqlStatement.executeUpdate(statement.statement());
                }
            } catch (SQLException exception) {
                LOGGER.error(
                    "Statement id {} \"{}\" on database {} with action {} caused {}",
                    statement.id(), statement.statement(), statement.database(), statement.action(), exception
                );
                throw new RuntimeException(exception);
            }
        });
    }

    public void queueStatement(@NotNull DatabaseType database, @NotNull String statement) {
        queueStatement(new SimpleStatement(SimpleStatement.Action.WRITE, database, statement));
    }
}
