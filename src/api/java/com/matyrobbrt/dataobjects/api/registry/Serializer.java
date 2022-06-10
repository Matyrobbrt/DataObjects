package com.matyrobbrt.dataobjects.api.registry;

import com.google.gson.JsonObject;

import java.lang.reflect.Type;

public interface Serializer<T> {
    T deserialize(JsonObject json);
    JsonObject serialize(T object);

    record Gson<T>(com.google.gson.Gson gson, Type type) implements Serializer<T> {
        @Override
        public T deserialize(JsonObject json) {
            return gson.fromJson(json, type);
        }

        @Override
        public JsonObject serialize(T object) {
            return (JsonObject) gson.toJsonTree(object, type);
        }
    }
}
