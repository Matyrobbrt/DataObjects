package com.matyrobbrt.dataobjects.defaults.item;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import com.matyrobbrt.dataobjects.api.registry.RegistryBuilder;
import com.matyrobbrt.dataobjects.api.registry.Serializer;
import com.matyrobbrt.dataobjects.defaults.CreativeModeTabRegistry;
import com.matyrobbrt.dataobjects.gson.EnumAdapter;
import com.matyrobbrt.dataobjects.gson.ObjectReference;
import com.matyrobbrt.dataobjects.gson.RLAdapter;
import com.matyrobbrt.dataobjects.gson.object.JsonEffect;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.Map;

public class ItemCreator {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new RLAdapter())
            .registerTypeAdapter(Rarity.class, new EnumAdapter<>(Rarity.class))
            .registerTypeAdapter(new TypeToken<ObjectReference<CreativeModeTab>>() {}.getType(), ObjectReference.createAdapter(CreativeModeTabRegistry.REGISTRY))
            .registerTypeAdapter(new TypeToken<ObjectReference<MobEffect>>() {}.getType(), ObjectReference.createAdapter(Registry.MOB_EFFECT))
            .registerTypeAdapter(new TypeToken<ObjectReference<Item>>() {}.getType(), ObjectReference.createAdapter(Registry.ITEM))
            .setPrettyPrinting()
            .setPrettyPrinting()
            .create();

    @DataObjectRegistry.Register
    public static final DataObjectRegistry<Item, ItemCreator> REGISTRY = RegistryBuilder.<Item, ItemCreator>builder(Registry.ITEM_REGISTRY)
            .defaultFactory((creator, json) -> new Item(creator.build()))
            .serializer(new Serializer.Gson<>(GSON, ItemCreator.class))
            .build();

    public int maxStackSize = 64;
    public Rarity rarity = Rarity.COMMON;
    @SerializedName("tab")
    public ObjectReference<CreativeModeTab> creativeModeTab;
    public boolean fireResistant = false;
    public int maxDamage = -1;
    public boolean canRepair;
    @SerializedName("food")
    public FoodCreator foodProperties;
    public ObjectReference<Item> craftRemainder;

    public CreativeModeTab getTab() {
        try {
            return creativeModeTab.get();
        } catch (NullPointerException e) {
            // Due to the fact we may not catch all tabs when we initially register, let's see if we can re-register the missing ones.
            for (final var tab : CreativeModeTab.TABS) {
                final var id = new ResourceLocation(tab.getRecipeFolderName().replace('.', ':'));
                if (!CreativeModeTabRegistry.REGISTRY.containsKey(id))
                    Registry.register(CreativeModeTabRegistry.REGISTRY, id, tab);
            }
            return creativeModeTab.get(); // It will re-lookup
        }
    }

    public Item.Properties build() {
        final var props = new Item.Properties().stacksTo(maxStackSize).tab(getTab()).rarity(rarity);
        if (fireResistant)
            props.fireResistant();
        if (maxDamage > -1)
            props.durability(maxDamage);
        if (!canRepair)
            props.setNoRepair();
        if (foodProperties != null)
            props.food(foodProperties.build());
        if (craftRemainder != null)
            props.craftRemainder(craftRemainder.get());
        return props;
    }


    public static final class FoodCreator {

        public int nutrition;
        public float saturationModifier;
        public boolean isMeat;
        public boolean canAlwaysEat;
        public boolean fastFood;
        public EffectInstance[] effects;

        public FoodProperties build() {
            final var builder = new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturationModifier);
            if (isMeat) builder.meat();
            if (canAlwaysEat) builder.alwaysEat();
            if (fastFood) builder.fast();
            for (final var effect : effects)
                builder.effect(effect.effect::build, effect.probability);
            return builder.build();
        }

        public static final class EffectInstance {
            private final JsonEffect effect;
            private final float probability;

            public EffectInstance(JsonEffect effect, float probability) {
                this.effect = effect;
                this.probability = probability;
            }
        }
    }
}
