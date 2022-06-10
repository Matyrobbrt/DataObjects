package com.matyrobbrt.dataobjects.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.matyrobbrt.dataobjects.api.mod.Mod;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import com.matyrobbrt.dataobjects.api.registry.ObjectFactory;
import com.matyrobbrt.dataobjects.api.script.ScriptContext;
import com.matyrobbrt.dataobjects.api.script.ScriptRunner;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.ServiceLoader;

/**
 * The main class used for interacting with DataObject's API. <br>
 * An instance of this interface can be got from {@link DataObjectsAPI#INSTANCE}.
 */
public interface DataObjectsAPI {
    /**
     * The mod ID of DataObjects.
     */
    String MOD_ID = "dataobjects";
    /**
     * The pack location of DataObjects.
     */
    String RESOURCE_LOCATION = "dataobjects";
    /**
     * The name of the factory json property.
     */
    String FACTORY_OBJECT_NAME = "factory";

    /**
     * The singleton instance of the API.
     */
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

    /**
     * Processes a FileEntry, and defers the registration of the object created with it for later.
     *
     * @param entry    the entry to process
     * @param registry the registry to register the object to at a later date
     */
    void processEntry(Mod.FileEntry entry, DataObjectRegistry<?, ?> registry) throws IOException;

    /**
     * Registers a {@link DataObjectRegistry}.
     *
     * @param registry the registry to register
     * @param <T>      the top level type of the registry
     * @param <C>      the type of the object creator
     * @return the registered registry
     */
    @CanIgnoreReturnValue
    <T, C> DataObjectRegistry<T, C> register(DataObjectRegistry<T, C> registry);

    /**
     * Registers a {@link ObjectFactory factory} for a DataObject registry.
     *
     * @param factoryName the name of the factory. It is <strong><i>strongly</i></strong> recommended that the name of your factory is prefixed with
     *                    your mod id ({@code examplemod.exampleitem}).
     * @param factory     the factory
     * @param registry    the {@link DataObjectRegistry#getResourceKey() resource key} of the registry to bind this factory to
     * @param <T>         the top level type of the registry
     * @param <Z>         the object creator of the registry
     * @return the registered factory
     */
    @CanIgnoreReturnValue
    <T, Z> ObjectFactory<? extends T, Z> registerFactory(String factoryName, ObjectFactory<? extends T, Z> factory, ResourceLocation registry);

    /**
     * Registers a {@link ObjectFactory factory} for a DataObject registry.
     *
     * @param factoryName the name of the factory. It is <strong><i>strongly</i></strong> recommended that the name of your factory is prefixed with
     *                    your mod id ({@code examplemod.exampleitem}).
     * @param factory     the factory
     * @param registry    the registry to bind this factory to
     * @param <T>         the top level type of the registry
     * @param <Z>         the object creator of the registry
     * @return the registered factory
     */
    @CanIgnoreReturnValue
    default <T, Z> ObjectFactory<? extends T, Z> registerFactory(String factoryName, ObjectFactory<? extends T, Z> factory, DataObjectRegistry<T, Z> registry) {
        return registerFactory(factoryName, factory, registry.getResourceKey().location());
    }

    /**
     * Gets the currently registered {@link DataObjectRegistry object registries}.
     *
     * @return the currently registered {@link DataObjectRegistry object registries}
     */
    Collection<DataObjectRegistry<?, ?>> getRegistries();

    // Scripts

    /**
     * Gets the interface used for scripting.
     *
     * @return the interface used for scripting
     */
    Scripts scripts();

    /**
     * The main interface used for scripting. <br>
     * An instance of this interface can be got from {@link DataObjectsAPI#scripts()}.
     */
    interface Scripts {
        /**
         * Registers a {@link ScriptRunner} to the runner register.
         *
         * @param runner the runner to register
         */
        void registerRunner(ScriptRunner runner);

        /**
         * Gets a runner for a script file extension
         *
         * @param extension the extension of the script (e.g. js, groovy, etc.)
         * @return the runner, or {@code null} if one doesn't exist for that extension
         */
        @Nullable
        ScriptRunner getRunner(String extension);

        /**
         * Creates a {@link ScriptContext}.
         *
         * @param script the script the context holds
         * @return the context
         */
        ScriptContext createContext(String script);
    }
}
