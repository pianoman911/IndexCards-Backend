package de.pianoman911.indexcards.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDSerializer implements TypeSerializer<UUID> {

    public static UUIDSerializer INSTANCE = new UUIDSerializer();

    private UUIDSerializer() {
    }

    @Override
    public UUID deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return node.virtual() ? null : UUID.fromString(node.getString(""));
    }

    @Override
    public void serialize(Type type, @Nullable UUID obj, ConfigurationNode node) throws SerializationException {
        node.set(obj == null ? null : obj.toString());
    }
}
