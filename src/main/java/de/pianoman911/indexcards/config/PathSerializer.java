package de.pianoman911.indexcards.config;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Objects;

public class PathSerializer implements TypeSerializer<Path> {

    public static final PathSerializer INSTANCE = new PathSerializer();

    private PathSerializer() {
    }

    @Override
    public Path deserialize(Type type, ConfigurationNode node) {
        return node.virtual() ? null : Path.of(Objects.requireNonNull(node.getString()));
    }

    @Override
    public void serialize(Type type, @Nullable Path obj, ConfigurationNode node) throws SerializationException {
        node.set(obj == null ? null : obj.toString());
    }
}
