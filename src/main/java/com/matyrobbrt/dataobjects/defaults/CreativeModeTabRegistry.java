package com.matyrobbrt.dataobjects.defaults;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matyrobbrt.dataobjects.util.Utils;
import com.matyrobbrt.dataobjects.api.DataObjectsAPI;
import com.matyrobbrt.dataobjects.api.registry.DataObjectRegistry;
import com.matyrobbrt.dataobjects.api.registry.RegistryBuilder;
import com.matyrobbrt.dataobjects.api.registry.Serializer;
import com.matyrobbrt.dataobjects.gson.ObjectReference;
import com.matyrobbrt.dataobjects.gson.RLAdapter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeModeTabRegistry {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new RLAdapter())
            .registerTypeAdapter(new TypeToken<ObjectReference<Item>>() {}.getType(), ObjectReference.createAdapter(Registry.ITEM))
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();

    public static final ResourceKey<Registry<CreativeModeTab>> RESOURCE_KEY = ResourceKey.createRegistryKey(new ResourceLocation(DataObjectsAPI.MOD_ID, "creative_mode_tab"));
    public static final Registry<CreativeModeTab> REGISTRY = Utils.make(RESOURCE_KEY);
    @DataObjectRegistry.Register
    public static final DataObjectRegistry<CreativeModeTab, Creator> DATA_OBJECT_REGISTRY = new RegistryBuilder<CreativeModeTab, Creator>(RESOURCE_KEY)
            .defaultFactory((creator, factoryObject) -> creator.build())
            .registerMethod(context -> context.register(context.getKey(), () -> context.create().setRecipeFolderName(context.getKey().getPath())))
            .serializer(new Serializer.Gson<>(GSON, Creator.class))
            .build();

    public static final class Creator {

        public String label = "plsChangeMe";
        public ResourceLocation backgroundImage;
        public ObjectReference<Item> icon = ObjectReference.create(new ResourceLocation("air"), Registry.ITEM);

        public CreativeModeTab build() {
            return new CreativeModeTab(label) {
                @Override
                public @NotNull ItemStack makeIcon() {
                    return new ItemStack(icon.get());
                }

                @Override
                public @NotNull ResourceLocation getBackgroundImage() {
                    return backgroundImage == null ? super.getBackgroundImage() : backgroundImage;
                }
            };
        }

    }
}
