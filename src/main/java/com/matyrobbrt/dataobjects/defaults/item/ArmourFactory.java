package com.matyrobbrt.dataobjects.defaults.item;

import com.google.gson.JsonObject;
import com.matyrobbrt.dataobjects.api.registry.BindFactory;
import com.matyrobbrt.dataobjects.api.registry.ObjectFactory;
import com.matyrobbrt.dataobjects.util.JsonUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

import java.util.Objects;

@BindFactory(value = "armor", registry = "item")
public class ArmourFactory implements ObjectFactory<ArmorItem, ItemCreator> {

    @Override
    public ArmorItem create(ItemCreator creator, JsonObject factoryObject) {
        if (factoryObject == null)
            throw new UnsupportedOperationException("Cannot create armor item of unknown type!");
        final var slot = JsonUtils.getEnum(factoryObject, "equipmentSlot", EquipmentSlot.class,
                EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.HEAD, EquipmentSlot.LEGS);
        final var type = JsonUtils.getRL(factoryObject, "material");
        final var material = ArmorMaterialRegistry.REGISTRY.get(type);
        return new ArmorItem(Objects.requireNonNull(material), slot, creator.build());
    }
}
