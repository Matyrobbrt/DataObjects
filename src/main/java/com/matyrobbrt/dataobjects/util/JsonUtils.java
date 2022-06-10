package com.matyrobbrt.dataobjects.util;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public class JsonUtils {
    public static ResourceLocation getRL(JsonObject json, String name) {
        final var str = json.get(name);
        if (str == null)
            throw new NullPointerException("Missing required property: " + name);
        return new ResourceLocation(str.getAsString());
    }

    public static <T extends Enum<T>> T getEnum(JsonObject jsonObject, String name, Class<T> clazz, T... allowedValues) {
        final var js = jsonObject.get(name);
        if (js == null)
            throw new NullPointerException("Missing required property: " + name);
        final var str = js.getAsString();
        for (final var t : (allowedValues.length < 1 ? clazz.getEnumConstants() : allowedValues))
            if (t.toString().equalsIgnoreCase(str))
                return t;
        throw new IllegalArgumentException("Unknown value: '" + str + "'");
    }
}
