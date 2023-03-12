package de.pianoman911.indexcards.sql;

import com.google.common.base.Preconditions;
import de.pianoman911.indexcards.IndexCards;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DatabaseType {

    public static final Map<String, DatabaseType> MAP = new HashMap<>();

    public static final DatabaseType BASIC = registerType(null, new DatabaseType("basic"));
    public static final DatabaseType USER = registerType(null, new DatabaseType("user"));
    public static final DatabaseType CARD = registerType(null, new DatabaseType("card"));

    @Deprecated(forRemoval = true)
    public static void register(DatabaseType type) {
        registerType(null, type);
    }

    public static DatabaseType registerType(@Nullable IndexCards service, DatabaseType type) {
        Preconditions.checkState(!MAP.containsKey(type.lowerName()), "type " + type + " already registered");
        MAP.put(type.lowerName(), type);

        if (service != null) {
            type.loadCredentials(service);
        }

        return type;
    }

    public static @Nullable DatabaseType valueOf(String name) {
        return MAP.get(name.toLowerCase(Locale.ROOT));
    }

    private final String upperName, lowerName;
    private Credentials credentials;

    public DatabaseType(String name) {
        this.upperName = name.toUpperCase();
        this.lowerName = name.toLowerCase();
    }

    public void loadCredentials(IndexCards service) {
        try (ResultSet result = service.queue().read(DatabaseType.BASIC, "SELECT `hostname`, " +
                "`database`, `username`, `password`, `port` FROM `sql` WHERE `type_name` = '" + this.lowerName + "'")) {
            if (result.next()) {
                this.credentials(new Credentials(result.getString("database"),
                        result.getString("username"), result.getString("password"),
                        result.getString("hostname"), result.getInt("port")));
            }
        } catch (SQLException exception) {
            throw new Error(exception);
        }
        service.sql().connect(new ConnectionFactory(this));
    }

    public String upperName() {
        return this.upperName;
    }

    public String lowerName() {
        return this.lowerName;
    }

    @Contract(pure = true)
    public @Nullable Credentials credentials() {
        return this.credentials;
    }

    @ApiStatus.Internal
    public void credentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return this.upperName;
    }
}
