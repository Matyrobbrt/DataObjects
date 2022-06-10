package com.matyrobbrt.dataobjects.api.mod;

import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface Mod {

    /**
     * Gets all the namespaces this mod has DataObject entries for.
     *
     * @return all the namespaces this mod has DataObject entries for
     */
    List<String> getNamespaces();

    /**
     * Gets all the entries this mod has for a {@link DataObjectRegistry registry}.
     *
     * @param registry the registry
     * @return the entries
     */
    Collection<FileEntry> getEntries(DataObjectRegistry<?, ?> registry) throws IOException;

    record FileEntry(ResourceLocation location, Resource.IoSupplier<InputStream> getter) {
        public InputStream open() throws IOException {
            return getter().get();
        }
    }

}
