package com.matyrobbrt.dataobjects.pack;

import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class DOResourceManager {

    private final RepositorySource folderPackFinder;

    public DOResourceManager() {
        folderPackFinder = new FolderRepositorySource(getLocation(), PackSource.DEFAULT);
    }

    public static File getLocation() {
        final var location = FMLPaths.GAMEDIR.get().resolve("dataobjects").toFile();
        if (!location.exists() && !location.mkdirs())
            throw new RuntimeException("Could not create dataobjects directory! Please create the directory yourself, or make sure the name is not taken by a file and you have permission to create directories.");
        return location;
    }

    public RepositorySource getWrappedPackFinder() {
        return (infoConsumer, infoFactory) -> folderPackFinder.loadPacks(infoConsumer::accept, (a, n, b, c, d, e, f, g) ->
                infoFactory.create("dataobjects:" + a, n, true, c, d, e, f, g));
    }

}
