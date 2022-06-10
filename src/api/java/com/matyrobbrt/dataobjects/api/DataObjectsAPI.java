package com.matyrobbrt.dataobjects.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.matyrobbrt.dataobjects.api.mod.Mod;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import com.matyrobbrt.dataobjects.api.registry.ObjectFactory;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.Collection;
import java.util.ServiceLoader;

// TODO document everything!
public interface DataObjectsAPI {
    String MOD_ID = "dataobjects";
    String RESOURCE_LOCATION = "dataobjects";
    String FACTORY_OBJECT_NAME = "factory";

    DataObjectsAPI INSTANCE = Util.make(() -> {
        final var loader = ServiceLoader.load(DataObjectsAPI.class).iterator();
        if (!loader.hasNext()) {
            throw new NullPointerException("No DataObjectsAPI was found on the classpath");
        }
        final var api = loader.next();
        if (loader.hasNext()) {
            throw new IllegalArgumentException("More than one DataObjectsAPI was found!");
        }
        return api;
    });

    void processEntry(Mod.FileEntry entry, DataObjectRegistry<?, ?> registry) throws IOException;

    @CanIgnoreReturnValue
    <T, C> DataObjectRegistry<T, C> register(DataObjectRegistry<T, C> registry);

    @CanIgnoreReturnValue
    <T, Z> ObjectFactory<? extends T, Z> registerFactory(String factoryName, ObjectFactory<? extends T, Z> factory, ResourceLocation registry);
    @CanIgnoreReturnValue
    default <T, Z> ObjectFactory<? extends T, Z> registerFactory(String factoryName, ObjectFactory<? extends T, Z> factory, DataObjectRegistry<T, Z> registry) {
        return registerFactory(factoryName, factory, registry.getResourceKey().location());
    }

    Collection<DataObjectRegistry<?, ?>> getRegistries();
}
