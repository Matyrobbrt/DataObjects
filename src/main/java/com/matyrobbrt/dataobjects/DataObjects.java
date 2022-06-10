package com.matyrobbrt.dataobjects;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.matyrobbrt.dataobjects.api.DataObjectsAPI;
import com.matyrobbrt.dataobjects.api.registry.BindFactory;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import com.matyrobbrt.dataobjects.api.registry.ObjectFactory;
import com.matyrobbrt.dataobjects.impl.APIImpl;
import com.matyrobbrt.dataobjects.impl.ModImpl;
import com.matyrobbrt.dataobjects.pack.DOResourceManager;
import com.matyrobbrt.dataobjects.util.Utils;
import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.impl.Jar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.module.ModuleDescriptor;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Manifest;

@Mod(DataObjectsAPI.MOD_ID)
@SuppressWarnings("DuplicatedCode")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DataObjectsAPI.MOD_ID)
public class DataObjects {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataObjectsAPI.MOD_ID);

    final DOResourceManager resourceManager;

    public DataObjects() {
        FMLJavaModLoadingContext.get().getModEventBus().register(DataObjectsAPI.INSTANCE);

        this.resourceManager = new DOResourceManager();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addPackFinders);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            doClient();
        }
    }

    private void doClient() {
        new DataObjectsClient(this);
    }

    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA)
            event.addRepositorySource(resourceManager.getWrappedPackFinder());
    }

    private static Multimap<Type, ModFileScanData.AnnotationData> foundAnnotations;

    @SubscribeEvent
    static void onConstruct(final FMLConstructModEvent event) {
        foundAnnotations = ModList.get().getAllScanData().stream()
                .map(ModFileScanData::getAnnotations)
                .flatMap(Collection::stream)
                .filter(a -> a.annotationType().equals(DataObjectRegistry.Register.TYPE) || a.annotationType().equals(BindFactory.TYPE))
                .collect(Multimaps.toMultimap(
                        ModFileScanData.AnnotationData::annotationType,
                        Function.identity(),
                        MultimapBuilder.hashKeys().arrayListValues()::build
                ));
        final Map<Type, Class<?>> classCache = new HashMap<>();
        try {
            collectRegistries(classCache);
        } catch (Throwable e) {
            LOGGER.error("Exception trying to collect DataObject registries: ", e);
            throw new RuntimeException(e);
        }
        try {
            bindFactories(classCache);
        } catch (Throwable e) {
            LOGGER.error("Exception trying to collect DataObject factories: ", e);
            throw new RuntimeException(e);
        }

        try {
            handleRegistering();
        } catch (Exception e) {
            LOGGER.error("Exception trying to register data objects: ", e);
            throw new RuntimeException(e);
        }
    }

    private static void collectRegistries(Map<Type, Class<?>> classCache) throws Exception {
        LOGGER.info("Registering DataObject registries...");
        for (final var data : foundAnnotations.get(DataObjectRegistry.Register.TYPE)) {
            Class<?> clazz = classCache.get(data.clazz());
            if (clazz == null) {
                clazz = Class.forName(data.clazz().getClassName(), false, DataObjects.class.getClassLoader());
                classCache.put(data.clazz(), clazz);
            }
            final var f = clazz.getDeclaredField(data.memberName());
            final var obj = f.get(null);
            if (!(obj instanceof DataObjectRegistry<?,?> registry)) {
                throw new IllegalArgumentException("Found field '%s' annotated with @DataObjectRegistry.Register that is not a DataObjectRegistry.".formatted(f));
            }
            DataObjectsAPI.INSTANCE.register(registry);
        }
        LOGGER.info("Finished registering DataObject registries.");
    }

    private static void bindFactories(Map<Type, Class<?>> classCache) throws Exception {
        LOGGER.info("Registering DataObject factories...");
        for (final var data : foundAnnotations.get(BindFactory.TYPE)) {
            Class<?> clazz = classCache.get(data.clazz());
            if (clazz == null) {
                clazz = Class.forName(data.clazz().getClassName(), false, DataObjects.class.getClassLoader());
                classCache.put(data.clazz(), clazz);
            }
            final var isField = data.targetType() == ElementType.FIELD;
            ObjectFactory<?, ?> factory;
            if (isField) {
                final var f = clazz.getDeclaredField(data.memberName());
                final var obj = f.get(null);
                if (!(obj instanceof ObjectFactory<?,?> fct)) {
                    throw new IllegalArgumentException("Found field '%s' annotated with @BindFactory that is not an ObjectFactory.".formatted(f));
                }
                factory = fct;
            } else {
                final var constructor = clazz.getDeclaredConstructor();
                final var obj = constructor.newInstance();
                if (!(obj instanceof ObjectFactory<?,?> fct)) {
                    throw new IllegalArgumentException("Found class '%s' annotated with @BindFactory that is not an ObjectFactory.".formatted(clazz));
                }
                factory = fct;
            }

            DataObjectsAPI.INSTANCE.registerFactory(
                    data.annotationData().get("value").toString(),
                    factory,
                    new ResourceLocation(data.annotationData().get("registry").toString())
            );
        }
        LOGGER.info("Finished registering DataObject factories.");
    }

    private static void handleRegistering() throws Exception {
        LOGGER.info("Collecting DataObjects...");
        final var modFiles = ModList.get().getModFiles();
        for (final var modFile : modFiles) {
            final var mod = new ModImpl(modFile.getFile());
            if (mod.getNamespaces().isEmpty())
                continue;
            for (final var registry : DataObjectsAPI.INSTANCE.getRegistries()) {
                final var entries = mod.getEntries(registry);
                for (final var entry : entries)
                    DataObjectsAPI.INSTANCE.processEntry(entry, registry);
            }
        }

        final var location = DOResourceManager.getLocation().toPath();
        processDir(location.resolve(DataObjectsAPI.RESOURCE_LOCATION), paths -> location.resolve(String.join("/", paths)));
        Files.walkFileTree(location, Set.of(), 1, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final var name = file.getFileName().toString();
                if (name.endsWith(".zip")) {
                    // We want to load files from zip packs as well
                    // Files.walkFileTree on a ZipFileSystem seems to act funky. If MML
                    // gives us the Jar class which we can use to access the sub directories,
                    // why not use it?
                    final var jar = new Jar(Manifest::new, sj -> new JarMetadata() {

                        @Override
                        public String name() {
                            return "sup?";
                        }

                        @Override
                        public String version() {
                            return "MIT gud";
                        }

                        @Override
                        public ModuleDescriptor descriptor() {
                            return ModuleDescriptor.newAutomaticModule("hello there").build();
                        }
                    }, (p1, p2) -> true, file.toAbsolutePath());
                    processDir(jar.getPath(DataObjectsAPI.RESOURCE_LOCATION), paths -> jar.getPath(String.join("/", paths)));
                }
                return super.visitFile(file, attrs);
            }
        });

        LOGGER.info("Finished collecting DataObjects.");
    }

    private static void processDir(Path dir, Function<List<String>, Path> pathGetter) throws IOException {
        final var modIds = Utils.findNamespaces(dir);
        for (final var namespace : modIds) {
            for (final var registry : DataObjectsAPI.INSTANCE.getRegistries()) {
                final var path = Utils.buildPath(registry.getResourceKey().location());
                path.add(0, DataObjectsAPI.RESOURCE_LOCATION);
                path.add(1, namespace);
                final var dirPath = pathGetter.apply(path);
                if (Files.exists(dirPath))
                    Files.walkFileTree(dirPath, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            final var relative = dirPath.relativize(file).toString();
                            if (relative.endsWith(".json")) {
                                final var entry = new com.matyrobbrt.dataobjects.api.mod.Mod.FileEntry(new ResourceLocation(namespace, relative.substring(0, relative.length() - 5)), // Strip the extension
                                        () -> Files.newInputStream(file));
                                APIImpl.instance.processEntry(entry, registry, true); // We want to allow that folder to override
                            }
                            return super.visitFile(file, attrs);
                        }
                    });
            }
        }
    }

}
