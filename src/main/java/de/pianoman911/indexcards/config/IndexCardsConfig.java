package de.pianoman911.indexcards.config;

import de.pianoman911.indexcards.sql.Credentials;
import de.pianoman911.indexcards.util.CipherUtils;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.net.InetSocketAddress;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class IndexCardsConfig {

    @Comment("The port the server should listen on")
    public int port = 8080;

    @Comment("MySQL connection credentials")
    public MySql mysql = new MySql();

    @Comment("The origin of the webserver (for CORS)")
    public String origin = "https://localhost:8080";

    @Comment("The secret key used to encrypt the passwords")
    public String key = CipherUtils.byteToString(CipherUtils.generateKey(), false);

    @ConfigSerializable
    public static class MySql {

        @Comment("The address of your MySQL/MariaDB service")
        public InetSocketAddress address = new InetSocketAddress("localhost", 3306);

        @Comment("The database name of the main database")
        public String database = "mysql";

        @Comment("The username of your mysql user")
        public String username = "root";

        @Comment("The password to login into your mysql service")
        public String password = "";

        public Credentials buildCredentials() {
            return new Credentials(this.database, this.username, this.password,
                    this.address.getAddress().getHostAddress(), this.address.getPort());
        }
    }
}
