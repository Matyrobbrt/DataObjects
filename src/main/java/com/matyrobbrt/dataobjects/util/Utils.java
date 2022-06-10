package com.matyrobbrt.dataobjects.util;

import com.google.common.collect.Lists;
import com.matyrobbrt.dataobjects.api.DataObjectsAPI;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Utils {
    public static List<String> findNamespaces(Path location) throws IOException {
        if (Files.exists(location))
            return Files.walk(location, 1)
                    .map(location::relativize)
                    .filter(path -> path.getNameCount() > 0) // skip the root entry
                    .map(p->p.toString().replaceAll("/$","")) // remove the trailing slash, if present
                    .filter(s -> !s.isEmpty()) //filter empty strings, otherwise empty strings default to minecraft in ResourceLocations
                    .toList();
        return List.of();
    }

    public static List<String> buildPath(ResourceLocation location) {
        return location.getNamespace().equals("minecraft") || location.getNamespace().equals(DataObjectsAPI.RESOURCE_LOCATION) ?
                Lists.newArrayList(location.getPath()) :
                Lists.newArrayList(location.getNamespace(), location.getPath());
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> make(ResourceKey<? extends Registry<T>> key) {
        return (Registry<T>) Registry.<Registry<?>>register((Registry<? super Registry<?>>) Registry.REGISTRY, key.location().toString(), new MappedRegistry<>(key, Lifecycle.experimental(), null));
    }
}
