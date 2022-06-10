package com.matyrobbrt.dataobjects.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface DataObjectRegistry<T, C> {
    ResourceKey<? extends Registry<T>> getResourceKey();

    T register(CreationContext<T> context);

    @NotNull
    Serializer<C> getSerializer();

    @Nullable
    ObjectFactory<T, C> getDefaultFactory();

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Register {
        Type TYPE = Type.getType(Register.class);
    }
}
