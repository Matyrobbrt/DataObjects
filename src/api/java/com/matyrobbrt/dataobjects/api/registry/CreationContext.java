package com.matyrobbrt.dataobjects.api.registry;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Represents a context used for creating objects.
 *
 * @param <T> the type of the object to create
 */
public interface CreationContext<T> {
    /**
     * Creates the object.
     *
     * @return the object
     */
    T create();

    /**
     * Gets the key / registry name of the created object.
     *
     * @return the key / registry name of the created object
     */
    ResourceLocation getKey();

    /**
     * Gets the json used for creating the object.
     *
     * @return the json used for creating the object
     */
    JsonObject getJson();

    /**
     * Gets the factory used for creating the object.
     *
     * @return the factory used for creating the object
     */
    ObjectFactory<? extends T, ?> getFactory();

    /**
     * Gets the json of the factory object.
     *
     * @return the json of the factory object, otherwise {@code null}
     */
    @Nullable
    JsonObject getFactoryJson();

    /**
     * Registers the object.
     *
     * @param name  the name of the object
     * @param value the object to register
     * @return the registered object
     */
    T register(ResourceLocation name, Supplier<? extends T> value);
}
