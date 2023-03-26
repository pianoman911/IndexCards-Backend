package de.pianoman911.indexcards.sql;

public record Credentials(String database, String username, String password, String hostname, int port) {

    @Override
    public String toString() {
        return "Credentials{" +
                "database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                '}';
    }
}
