package de.pianoman911.indexcards.sql;

public class Credentials {

    private final String database, username, password;
    private final String hostname;
    private final int port;

    public Credentials(String database, String username, String password, String hostname, int port) {
        this.database = database;
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
    }

    public String database() {
        return this.database;
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

    public String hostname() {
        return this.hostname;
    }

    public int port() {
        return this.port;
    }
}
