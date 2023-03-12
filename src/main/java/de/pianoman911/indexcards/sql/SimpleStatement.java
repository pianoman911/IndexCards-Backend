package de.pianoman911.indexcards.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SimpleStatement {

    private static long currentId;
    private final Action action;
    private final DatabaseType database;
    private final String statement;
    private final long id;

    public SimpleStatement(@NotNull Action action, @NotNull DatabaseType database, @NotNull String statement) {
        this.action = action;
        this.database = database;
        this.statement = statement;
        id = currentId += 1;
    }

    public @NotNull Action action() {
        return action;
    }

    public @NotNull DatabaseType database() {
        return database;
    }

    public @NotNull String statement() {
        return statement;
    }

    public long id() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return this == object || (object != null && getClass() == object.getClass() && id == ((SimpleStatement) object).id && action == ((SimpleStatement) object).action && Objects.equals(database, ((SimpleStatement) object).database) && Objects.equals(statement, ((SimpleStatement) object).statement));
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, database, statement, id);
    }

    @Override
    public @NotNull String toString() {
        return "SimpleStatement{action=" + action + ", database=" + database + ", statement='" + statement + '\'' + ", id=" + id + '}';
    }

    public enum Action {

        READ,
        WRITE,
    }
}
