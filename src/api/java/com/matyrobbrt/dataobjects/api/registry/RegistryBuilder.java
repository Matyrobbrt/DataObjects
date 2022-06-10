package com.matyrobbrt.dataobjects.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class RegistryBuilder<T, C> {
    private final ResourceKey<? extends Registry<T>> resourceKey;
    private ObjectFactory<T, C> defaultFactory;
    private Serializer<C> serializer;
    private Function<CreationContext<T>, T> registerMethod = context -> context.register(context.getKey(), context::create);

    public RegistryBuilder(ResourceKey<? extends Registry<T>> resourceKey) {
        this.resourceKey = resourceKey;
    }
    public static <T, C> RegistryBuilder<T, C> builder(ResourceKey<? extends Registry<T>> resourceKey) {
        return new RegistryBuilder<>(resourceKey);
    }

    public RegistryBuilder<T, C> defaultFactory(ObjectFactory<T, C> defaultFactory) {
        this.defaultFactory = defaultFactory;
        return this;
    }

    public RegistryBuilder<T, C> serializer(Serializer<C> serializer) {
        this.serializer = serializer;
        return this;
    }

    public RegistryBuilder<T, C> registerMethod(Function<CreationContext<T>, T> method) {
        this.registerMethod = method;
        return this;
    }

    public DataObjectRegistry<T, C> build() {
        return new DataObjectRegistry<>() {
            @Override
            public ResourceKey<? extends Registry<T>> getResourceKey() {
                return resourceKey;
            }

            @Override
            public T register(CreationContext<T> context) {
                return registerMethod.apply(context);
            }

            @Override
            public @NotNull Serializer<C> getSerializer() {
                return Objects.requireNonNull(serializer);
            }

            @Override
            public ObjectFactory<T, C> getDefaultFactory() {
                return defaultFactory;
            }
        };
    }
}
