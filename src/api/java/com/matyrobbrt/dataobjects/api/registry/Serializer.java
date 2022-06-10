package com.matyrobbrt.dataobjects.api.registry;

import com.google.gson.JsonObject;

import java.lang.reflect.Type;

/**
 * An interface used for serializing objects to and from json.
 *
 * @param <T> the type of the serializer
 */
public interface Serializer<T> {
    /**
     * Deserializes an object from json.
     *
     * @param json the json representation of the object
     * @return the object
     */
    T deserialize(JsonObject json);

    /**
     * Serializes an object to json.
     *
     * @param object the object to serialize
     * @return the json representation of the object
     */
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
