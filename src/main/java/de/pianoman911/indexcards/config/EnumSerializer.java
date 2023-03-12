package de.pianoman911.indexcards.config;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class EnumSerializer extends ScalarSerializer<Enum<?>> {

    public static final EnumSerializer INSTANCE = new EnumSerializer();

    private EnumSerializer() {
        super(new TypeToken<>() { /**/
        });
    }

    @Override
    public Enum<?> deserialize(Type type, Object obj) throws SerializationException {
        return Enum.valueOf(GenericTypeReflector.erase(type).asSubclass(Enum.class), obj.toString());
    }

    @Override
    protected Object serialize(Enum<?> item, Predicate<Class<?>> typeSupported) {
        return item.name();
    }
}
