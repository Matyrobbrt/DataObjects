package com.matyrobbrt.dataobjects.impl;

import com.matyrobbrt.dataobjects.util.Utils;
import com.matyrobbrt.dataobjects.api.DataObjectsAPI;
import com.matyrobbrt.dataobjects.api.mod.Mod;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ModImpl implements Mod {
    private final IModFile file;
    private final List<String> namespaces;

    public ModImpl(IModFile file) throws IOException {
        this.file = file;

        final var location = file.findResource(DataObjectsAPI.RESOURCE_LOCATION);
        this.namespaces = Utils.findNamespaces(location);
    }

    @Override
    public List<String> getNamespaces() {
        return namespaces;
    }

    @Override
    public Collection<FileEntry> getEntries(DataObjectRegistry<?, ?> registry) throws IOException {
        final var entries = new ArrayList<FileEntry>();
        for (final var namespace : namespaces) {
            final var path = Utils.buildPath(registry.getResourceKey().location());
            path.add(0, DataObjectsAPI.RESOURCE_LOCATION);
            path.add(1, namespace);
            final var dir = file.findResource(path.toArray(String[]::new));
            if (Files.exists(dir))
                Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        final var relative = dir.relativize(file).toString();
                        if (relative.endsWith(".json"))
                            entries.add(new FileEntry(new ResourceLocation(namespace, relative.substring(0, relative.length() - 5)), // Strip the extension
                                    () -> Files.newInputStream(file)));
                        return super.visitFile(file, attrs);
                    }
                });
        }
        return entries;
    }

}
