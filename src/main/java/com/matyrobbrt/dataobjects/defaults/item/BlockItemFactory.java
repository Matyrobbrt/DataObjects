package com.matyrobbrt.dataobjects.defaults.item;

import com.google.gson.JsonObject;
import com.matyrobbrt.dataobjects.api.registry.BindFactory;
import com.matyrobbrt.dataobjects.api.registry.ObjectFactory;
import com.matyrobbrt.dataobjects.util.JsonUtils;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@BindFactory(value = "blockItem", registry = "item")
public class BlockItemFactory implements ObjectFactory<BlockItem, ItemCreator> {
    @Override
    public BlockItem create(ItemCreator creator, JsonObject factoryObject) {
        if (factoryObject == null)
            throw new UnsupportedOperationException("Cannot create block item without a parent!");
        return new BlockItem(
                Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(JsonUtils.getRL(factoryObject, "block"))),
                creator.build()
        );
    }
}
