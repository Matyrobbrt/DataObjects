package com.matyrobbrt.dataobjects.api.registry;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ObjectFactory<T, C> {
    T create(C creator, @Nullable JsonObject factoryObject);

    default void whenRegistered(T object, C creator, @Nullable JsonObject factoryObject) {

    }
}
