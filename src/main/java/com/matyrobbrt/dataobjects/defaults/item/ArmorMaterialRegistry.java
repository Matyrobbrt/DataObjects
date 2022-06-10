package com.matyrobbrt.dataobjects.defaults.item;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.matyrobbrt.dataobjects.api.DataObjectsAPI;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import com.matyrobbrt.dataobjects.api.registry.RegistryBuilder;
import com.matyrobbrt.dataobjects.api.registry.Serializer;
import com.matyrobbrt.dataobjects.gson.IngredientAdapter;
import com.matyrobbrt.dataobjects.gson.LazyValueAdapter;
import com.matyrobbrt.dataobjects.gson.ObjectReference;
import com.matyrobbrt.dataobjects.gson.RLAdapter;
import com.matyrobbrt.dataobjects.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class ArmorMaterialRegistry {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Ingredient.class, new IngredientAdapter())
            .registerTypeAdapter(new TypeToken<LazyLoadedValue<Ingredient>>() {}.getType(), new LazyValueAdapter<Ingredient>(Ingredient.class))
            .registerTypeAdapter(new TypeToken<ObjectReference<SoundEvent>>() {}.getType(), ObjectReference.createAdapter(Registry.SOUND_EVENT))
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    public static final ResourceKey<Registry<ArmorMaterial>> RESOURCE_KEY = ResourceKey.createRegistryKey(new ResourceLocation(DataObjectsAPI.MOD_ID, "armor_material"));
    public static final Registry<ArmorMaterial> REGISTRY = Utils.make(RESOURCE_KEY);
    @DataObjectRegistry.Register
    public static final DataObjectRegistry<ArmorMaterial, Creator> DATA_OBJECT_REGISTRY = new RegistryBuilder<ArmorMaterial, Creator>(RESOURCE_KEY)
            .defaultFactory((creator, factoryObject) -> creator.build())
            .serializer(new Serializer.Gson<>(GSON, Creator.class))
            .build();

    static {
        for (final var material : ArmorMaterials.values()) {
            Registry.register(REGISTRY, material.getName(), material);
        }
    }

    public static final class Creator {

        private static final int[] HEALTH_PER_SLOT = ObfuscationReflectionHelper.getPrivateValue(ArmorMaterials.class, null, "f_" + "40460_");

        public String name;
        public LazyLoadedValue<Ingredient> repairIngredient;
        public ObjectReference<SoundEvent> sound = ObjectReference.create(SoundEvents.ARMOR_EQUIP_GENERIC.getLocation(), Registry.SOUND_EVENT);
        public int durabilityMultiplier = 1;
        public int[] defense = {1, 1, 1, 1};
        public int enchantmentValue = 1;
        public float toughness;
        public float knockbackResistance;

        public ArmorMaterial build() {
            return new ArmorMaterial() {
                @Override
                public int getDurabilityForSlot(EquipmentSlot p_40410_) {
                    return HEALTH_PER_SLOT[p_40410_.getIndex()] * durabilityMultiplier;
                }

                @Override
                public int getDefenseForSlot(EquipmentSlot p_40411_) {
                    return defense[p_40411_.getIndex()];
                }

                @Override
                public int getEnchantmentValue() {
                    return enchantmentValue;
                }

                @Override
                public SoundEvent getEquipSound() {
                    return sound.get();
                }

                @Override
                public Ingredient getRepairIngredient() {
                    return repairIngredient.get();
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public float getToughness() {
                    return toughness;
                }

                @Override
                public float getKnockbackResistance() {
                    return knockbackResistance;
                }
            };
        }

    }
}
