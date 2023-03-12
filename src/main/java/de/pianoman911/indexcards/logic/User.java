package de.pianoman911.indexcards.logic;

import java.util.Objects;
import java.util.UUID;

public class User {

    private final String name;
    private final String password;
    private final UUID session = UUID.randomUUID();

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String name() {
        return name;
    }

    public String password() {
        return password;
    }

    public UUID session() {
        return session;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User other) {
            return Objects.equals(this.name, other.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
