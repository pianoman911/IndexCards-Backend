package de.pianoman911.indexcards.sql.queue;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.sql.DatabaseType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class QueueManager {

    private final ExecutorService writePool = newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("IndexCards SQL Write Queue #%d").build());
    private final ExecutorService readPool = newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("IndexCards SQL Read Queue #%d").build());
    private final WriteQueue writeQueue;
    private final ReadQueue readQueue;

    public QueueManager(@NotNull IndexCards service) {
        writeQueue = new WriteQueue(service);
        readQueue = new ReadQueue(service);
    }

    public @NotNull ExecutorService readPool() {
        return readPool;
    }

    public @NotNull ExecutorService writePool() {
        return writePool;
    }

    public @NotNull ResultSet read(@NotNull DatabaseType type, @NotNull String statement) {
        return readQueue.queueStatement(type, statement);
    }

    public @NotNull CompletableFuture<ResultSet> readAsync(@NotNull DatabaseType type, @NotNull String statement) {
        return readQueue.queueStatementAsync(type, statement);
    }

    public void write(@NotNull DatabaseType type, @NotNull String statement) {
        writeQueue.queueStatement(type, statement);
    }
}
