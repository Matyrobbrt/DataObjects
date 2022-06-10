package com.matyrobbrt.dataobjects.impl;

import com.google.auto.service.AutoService;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.matyrobbrt.dataobjects.api.DataObjectsAPI;
import com.matyrobbrt.dataobjects.api.mod.Mod;
import com.matyrobbrt.dataobjects.api.registry.CreationContext;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import com.matyrobbrt.dataobjects.api.registry.ObjectFactory;
import com.matyrobbrt.dataobjects.DataObjects;
import com.matyrobbrt.dataobjects.defaults.CreativeModeTabRegistry;
import com.matyrobbrt.dataobjects.defaults.item.ArmorMaterialRegistry;
import com.matyrobbrt.dataobjects.defaults.item.ItemCreator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@AutoService(DataObjectsAPI.class)
public class APIImpl implements DataObjectsAPI {

    private static final Map<Registry<?>, DataObjectRegistry<?, ?>> EARLY_REGISTRIES = Map.of(
            CreativeModeTabRegistry.REGISTRY, CreativeModeTabRegistry.DATA_OBJECT_REGISTRY,
            ArmorMaterialRegistry.REGISTRY, ArmorMaterialRegistry.DATA_OBJECT_REGISTRY
    );
    private static boolean didRegisterEarly;

    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setLenient()
            .create();
    private final Map<DataObjectRegistry<?, ?>, Map<ResourceLocation, JsonObject>> toRegister = new HashMap<>();
    private final Map<ResourceKey<?>, DataObjectRegistry<?, ?>> registries = new HashMap<>();
    private final BiMap<ResourceLocation, BiMap<String, ObjectFactory<?, ?>>> factories = HashBiMap.create();

    @Override
    public void processEntry(Mod.FileEntry entry, DataObjectRegistry<?, ?> registry) throws IOException {
         try (final var reader = new InputStreamReader(entry.open())) {
             final var json = gson.fromJson(reader, JsonObject.class);
             toRegister.computeIfAbsent(registry, k -> new HashMap<>()).put(entry.location(), json);
         }
    }

    @SubscribeEvent
    void onRegister(final RegisterEvent event) {
        if (!didRegisterEarly) {
            EARLY_REGISTRIES.forEach((objects, registry) -> doRegister(null, objects, registry));
            didRegisterEarly = true;
        }

        final var registry = registries.get(event.getRegistryKey());
        if (registry == null)
            return;
        doRegister(event.getForgeRegistry(), event.getVanillaRegistry(), registry);
    }

    private void doRegister(@Nullable IForgeRegistry<?> forgeRegistry, @Nullable Registry<?> vanillaRegistry, DataObjectRegistry<?, ?> registry) {
        DataObjects.LOGGER.info("Registering DataObjects for type {}... any 'potentially dangerous prefix' warnings can be safely ignored.", registry.getResourceKey());
        final var toRegister = this.toRegister.computeIfAbsent(registry, k -> new HashMap<>());
        for (final var entry : toRegister.entrySet()) {
            final var context = createContext(forgeRegistry, vanillaRegistry, entry.getKey(), registry, entry.getValue());
            register(registry, context);
        }

        DataObjects.LOGGER.info("Finished registering DataObjects for type {}.", registry.getResourceKey());
    }

    @SuppressWarnings("unchecked")
    private static <T, C> void register(DataObjectRegistry<T, C> registry, CreationContext<?> context) {
        registry.register((CreationContext<T>) context);
    }

    private <T, C> CreationContext<T> createContext(@Nullable IForgeRegistry<?> forgeRegistry, @Nullable Registry<?> vanillaRegistry, ResourceLocation key, DataObjectRegistry<T, C> registry, JsonObject jsonObject) {
        final var factoryObject = jsonObject.get(FACTORY_OBJECT_NAME);
        final var factory = factoryObject != null
                ? this.resolveFactory(key, registry.getDefaultFactory(), registry.getResourceKey().location(), factoryObject)
                : registry.getDefaultFactory();

        if (factory == null)
            throw new NullPointerException("DataObject '%s' of type '%s' must declare a valid factory.".formatted(
                    key, registry.getResourceKey().location()
            ));

        final var creator = registry.getSerializer().deserialize(jsonObject);

        return new CreationContext<>() {
            @Override
            public T create() {
                return factory.create(creator, getFactoryJson());
            }

            @Override
            public ResourceLocation getKey() {
                return key;
            }

            @Override
            public JsonObject getJson() {
                return jsonObject;
            }

            @Override
            public ObjectFactory<? extends T, ?> getFactory() {
                return factory;
            }

            @Override
            public @Nullable JsonObject getFactoryJson() {
                return factoryObject instanceof JsonObject json ? json : null;
            }

            @Override
            @SuppressWarnings({"rawtypes", "unchecked"})
            public T register(ResourceLocation name, Supplier<? extends T> value) {
                if (forgeRegistry != null)
                    ((IForgeRegistry) forgeRegistry).register(name, value.get());
                else if (vanillaRegistry != null)
                    Registry.register((Registry) vanillaRegistry, name, value.get());

                final var obj = (T) (vanillaRegistry == null ? forgeRegistry.getValue(name) : vanillaRegistry.get(name));
                whenRegistered(factory, obj, creator, getFactoryJson());
                return obj;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T, C> void whenRegistered(ObjectFactory<T, C> factory, Object obj, C creator, @Nullable JsonObject json) {
        factory.whenRegistered((T) obj, creator, json);
    }

    @SuppressWarnings("unchecked")
    private <T, C> ObjectFactory<? extends T, C> resolveFactory(ResourceLocation objectKey, ObjectFactory<T, C> defaultFactory, ResourceLocation registryKey, JsonElement jsonElement) {
        final var factories = this.factories.computeIfAbsent(registryKey, k -> HashBiMap.create());
        if (jsonElement instanceof JsonObject object) {
            if (!object.has("type")) {
                return defaultFactory;
            }
            final var type = object.get("type").getAsString();
            final var factory = factories.get(type);
            if (factory == null) {
                throw new IllegalArgumentException("DataObject '%s' pf type '%s' uses unknown factory: %s".formatted(
                        objectKey, registryKey, type
                ));
            }
            return (ObjectFactory<? extends T, C>) factory;
        }
        final var type = jsonElement.getAsString();
        final var factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("DataObject '%s' pf type '%s' uses unknown factory: %s".formatted(
                    objectKey, registryKey, type
            ));
        }
        return (ObjectFactory<? extends T, C>) factory;
    }

    @Override
    public <T, Z> ObjectFactory<? extends T, Z> registerFactory(String factoryName, ObjectFactory<? extends T, Z> factory, ResourceLocation registry) {
        factories.computeIfAbsent(registry, k -> HashBiMap.create()).put(factoryName, factory);
        return factory;
    }

    @Override
    public Collection<DataObjectRegistry<?, ?>> getRegistries() {
        return Collections.unmodifiableCollection(registries.values());
    }

    @Override
    public <T, C> DataObjectRegistry<T, C> register(DataObjectRegistry<T, C> registry) {
        final var key = registry.getResourceKey();
        if (registries.containsKey(key)) {
            throw new IllegalArgumentException("Tried to register DataObjects registry of type " + key + " more than once!");
        }
        registries.put(key, registry);
        registerFactory("default", registry.getDefaultFactory(), registry);
        return registry;
    }

}
