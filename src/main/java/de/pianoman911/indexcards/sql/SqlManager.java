package de.pianoman911.indexcards.sql;

import de.pianoman911.indexcards.IndexCards;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SqlManager {

    private static final Logger LOGGER = LogManager.getLogger(SqlManager.class);
    private final Map<DatabaseType, ConnectionFactory> factories = new HashMap<>();
    private final IndexCards service;

    public SqlManager(IndexCards service) {
        this.service = service;
    }

    @ApiStatus.Internal
    public void connect(ConnectionFactory factory) {
        if (this.factories.containsKey(factory.type())) {
            LOGGER.warn("Tried adding another ConnectionFactory for {}", factory.type());
        } else {
            LOGGER.info("Registered ConnectionFactory for {}", factory.type());
            this.factories.put(factory.type(), factory.init(this.service));
        }
    }

    public Connection connection(DatabaseType type) {
        return Objects.requireNonNull(this.factory(type),
                "no ConnectionFactory for " + type + " registered").connection();
    }

    public SqlManager connectBasic() {
        DatabaseType.BASIC.credentials(this.service.config().mysql.buildCredentials());
        this.connect(new ConnectionFactory(DatabaseType.BASIC));
        return this;
    }

    public SqlManager resolveCredentials() {
        for (DatabaseType type : DatabaseType.MAP.values()) {
            if (type.credentials() == null) {
                type.loadCredentials(this.service);
            }
        }
        return this;
    }


    public void shutdown() {
        for (ConnectionFactory factory : this.factories.values()) {
            factory.shutdown();
        }
    }

    public @Nullable ConnectionFactory factory(DatabaseType type) {
        return this.factories.get(type);
    }

}
