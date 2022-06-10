package com.matyrobbrt.dataobjects.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.Objects;

@MethodsReturnNonnullByDefault
public interface ObjectReference<T> {
    T get();

    ResourceLocation getLocation();

    static <Z> TypeAdapter<ObjectReference<Z>> createAdapter(Registry<Z> registry) {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, ObjectReference<Z> value) throws IOException {
                out.value(value.getLocation().toString());
            }

            @Override
            public ObjectReference<Z> read(JsonReader in) throws IOException {
                return create(new ResourceLocation(in.nextString()), registry);
            }
        };
    }

    static <T> ObjectReference<T> create(ResourceLocation location, Registry<T> registry) {
        return new ObjectReference<>() {
            T object;
            @Override
            public T get() {
                if (object == null)
                    object = registry.get(getLocation());
                return Objects.requireNonNull(object, "Object %s is not present in registry %s".formatted(location, registry));
            }

            @Override
            public ResourceLocation getLocation() {
                return location;
            }
        };
    }
}
