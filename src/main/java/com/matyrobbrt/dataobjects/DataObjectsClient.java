package com.matyrobbrt.dataobjects;

import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

public record DataObjectsClient(DataObjects dataObjects) {

    @SubscribeEvent
    void constructMod(FMLConstructModEvent event) {
        event.enqueueWork(() -> Minecraft.getInstance().getResourcePackRepository()
                .addPackFinder(dataObjects.resourceManager.getWrappedPackFinder()));
    }
}
