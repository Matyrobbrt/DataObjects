package com.matyrobbrt.dataobjects.api.registry;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a registry for DataObjects. <br>
 * A {@link DataObjectRegistry} needs to be registered to the {@link com.matyrobbrt.dataobjects.api.DataObjectsAPI#INSTANCE API}
 * using either {@link com.matyrobbrt.dataobjects.api.DataObjectsAPI#register(DataObjectRegistry)} or by annotating
 * the static field holding the registry with {@link Register}. <br>
 * The default implementation can be created using the {@link RegistryBuilder}.
 *
 * @param <T> the top level type of the registry
 * @param <C> the type of the object creator
 */
@MethodsReturnNonnullByDefault
public interface DataObjectRegistry<T, C> {
    /**
     * Gets the {@link ResourceKey resource key} of the wrapped {@link Registry registry}.
     *
     * @return {@link ResourceKey resource key} of the wrapped {@link Registry registry}
     */
    ResourceKey<? extends Registry<T>> getResourceKey();

    /**
     * Registers an object.
     *
     * @param context the context
     * @return the registered object
     */
    T register(CreationContext<T> context);

    /**
     * Gets the serializer used for serializing/deserializing a {@link C creator} to and from json.
     *
     * @return the creator serializer
     */
    Serializer<C> getSerializer();

    /**
     * Gets the default factory used for creating objects.
     *
     * @return the default factory used for creating objects, otherwise {@code null}
     */
    @Nullable
    ObjectFactory<T, C> getDefaultFactory();

    /**
     * Annotate a static field whose underlying type is a {@link DataObjectRegistry} with
     * this annotation in order to register it to the
     * {@link com.matyrobbrt.dataobjects.api.DataObjectsAPI#register(DataObjectRegistry) registry}.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Register {
        Type TYPE = Type.getType(Register.class);
    }
}
