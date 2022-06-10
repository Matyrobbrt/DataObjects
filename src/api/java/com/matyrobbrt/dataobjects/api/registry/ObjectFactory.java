package com.matyrobbrt.dataobjects.api.registry;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A functional interface used in order to create an object from a creator. <br>
 * By default, an object will be created using the {@link DataObjectRegistry#getDefaultFactory() default factory}.
 * If the object's json has a {@code factory} object, the factory will be: <br>
 * - If the {@code factory} object is a String, the factory with that value will be used. E.g. <pre>{@code "factory": "testFactory"}</pre> will make the factory with the name {@code testFactory} be used <br>
 * - If the {@code factory} object is a {@link JsonObject}, the name of the factory will be the {@code type} property, and the rest of the factory object will be passed to the factory. E.g.
 * <pre>
 *     {@code
 * "factory": {
 *   "type": "exampleFactory",
 *   "customProperty" 2
 * }}
 * </pre>
 * will use the factory with the name {@code exampleFactory} passing in the {@code factory} json object.
 * <br> <br>
 * A factory may be bound to a {@link DataObjectRegistry registry} using {@link com.matyrobbrt.dataobjects.api.DataObjectsAPI#registerFactory(String, ObjectFactory, ResourceLocation)}.
 *
 * @param <T> the type of the objects created by this factory
 * @param <C> the type of the creator
 */
@FunctionalInterface
public interface ObjectFactory<T, C> {
    /**
     * Creates an object.
     *
     * @param creator       the creator
     * @param factoryObject the json object of the factory, otherwise {@code null}
     * @return the created object
     */
    T create(C creator, @Nullable JsonObject factoryObject);

    /**
     * Called when an object is registered to allow for custom behaviour.
     *
     * @param object        the registered object
     * @param creator       the creator of the object
     * @param factoryObject the json object of the factory
     */
    default void whenRegistered(T object, C creator, @Nullable JsonObject factoryObject) {

    }
}
