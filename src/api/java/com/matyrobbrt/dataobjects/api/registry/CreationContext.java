package com.matyrobbrt.dataobjects.api.registry;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface CreationContext<T> {
    T create();

    ResourceLocation getKey();

    JsonObject getJson();
    ObjectFactory<? extends T, ?> getFactory();
    @Nullable
    JsonObject getFactoryJson();

    T register(ResourceLocation name, Supplier<? extends T> value);
}
