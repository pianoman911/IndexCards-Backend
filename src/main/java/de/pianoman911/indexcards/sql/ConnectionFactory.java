package de.pianoman911.indexcards.sql;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.pianoman911.indexcards.IndexCards;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

public class ConnectionFactory {

    private final DatabaseType type;
    private HikariDataSource hikari;

    @Deprecated(forRemoval = true)
    public ConnectionFactory(Credentials credentials, DatabaseType type) {
        this(type);
    }

    public ConnectionFactory(DatabaseType type) {
        this.type = type;
    }


    public ConnectionFactory init(IndexCards service) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("tjcproxy-hikari");

        Map<String, String> properties = new HashMap<>();

        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf8");
        properties.put("socketTimeout", String.valueOf(TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS)));

        String propertiesString = properties.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(";"));

        Credentials credentials = this.type.credentials();
        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        config.addDataSourceProperty("serverName", credentials.hostname());
        config.addDataSourceProperty("port", credentials.port());
        config.addDataSourceProperty("databaseName", credentials.database());
        config.addDataSourceProperty("properties", propertiesString);
        config.setUsername(credentials.username());
        config.setPassword(credentials.password());

        // won't work for the basic database type, but I don't care
        int poolSize = 5;

        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(poolSize);
        config.setMaxLifetime(300000);
        config.setKeepaliveTime(0);
        config.setConnectionTimeout(5000);
        config.setInitializationFailTimeout(-1);

        this.hikari = new HikariDataSource(config);
        return this;
    }

    public void shutdown() {
        if (this.hikari != null) {
            this.hikari.close();
            this.hikari = null;
        }
    }

    public Connection connection() {
        Preconditions.checkState(this.hikari != null, "ConnectionFactory not initialized, hikari is null");

        try {
            Connection connection = this.hikari.getConnection();
            if (connection != null) {
                return connection;
            }

            int maxWait = (1000 / 50) * 60; // 60 seconds, each loop takes 50ms
            while (connection == null && maxWait-- > 0) {
                LockSupport.parkNanos("Hikari Connection Pool Waiter", 1000 * 1000 * (1000 / 50));
                connection = this.hikari.getConnection();
            }

            Preconditions.checkState(connection != null, "Timed out after 60 seconds while waiting for hikari connection pool to return connection");
            return connection;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public DatabaseType type() {
        return this.type;
    }
}
