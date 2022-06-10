package com.matyrobbrt.dataobjects.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.util.LazyLoadedValue;

import java.lang.reflect.Type;

public record LazyValueAdapter<T>(Type type) implements JsonSerializer<LazyLoadedValue<T>>, JsonDeserializer<LazyLoadedValue<T>> {

    @Override
    public LazyLoadedValue<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new LazyLoadedValue<>(() -> context.deserialize(json, type));
    }

    @Override
    public JsonElement serialize(LazyLoadedValue<T> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.get(), type);
    }
}
