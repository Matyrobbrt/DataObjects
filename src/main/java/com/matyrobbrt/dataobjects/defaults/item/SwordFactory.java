package com.matyrobbrt.dataobjects.defaults.item;

import com.google.gson.JsonObject;
import com.matyrobbrt.dataobjects.api.registry.BindFactory;
import com.matyrobbrt.dataobjects.api.registry.ObjectFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.Objects;

@BindFactory(value = "sword", registry = "item")
public class SwordFactory implements ObjectFactory<SwordItem, ItemCreator> {

    @Override
    public SwordItem create(ItemCreator creator, JsonObject factoryObject) {
        if (factoryObject == null)
            throw new NullPointerException("Cannot create sword of unknown tier!");
        final var properties = ItemCreator.GSON.fromJson(factoryObject, SwordFactory.SwordProperties.class);
        return new SwordItem(Objects.requireNonNull(TierSortingRegistry.byName(properties.tier)), properties.attackDamage, properties.attackSpeed, creator.build());
    }

    public static class SwordProperties {
        public ResourceLocation tier;
        public int attackDamage = 10;
        public float attackSpeed = 10f;
    }
}
