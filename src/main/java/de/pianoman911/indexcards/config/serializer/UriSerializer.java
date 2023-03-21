package de.pianoman911.indexcards.config.serializer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.net.URI;

public class UriSerializer implements TypeSerializer<URI> {

    public static final UriSerializer INSTANCE = new UriSerializer();

    private UriSerializer() {
    }

    @Override
    public URI deserialize(Type type, ConfigurationNode node) {
        if (node.virtual()) return null;
        String url = node.getString("");
        if (!url.endsWith("/")) url += "/";
        return URI.create(url);
    }

    @Override
    public void serialize(Type type, @Nullable URI obj, ConfigurationNode node) throws SerializationException {
        node.set(obj == null ? null : obj.toString());
    }
}
