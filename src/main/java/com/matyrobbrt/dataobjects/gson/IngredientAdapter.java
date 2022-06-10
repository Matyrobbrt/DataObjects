package com.matyrobbrt.dataobjects.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.lang.reflect.Type;

public class IngredientAdapter implements JsonSerializer<Ingredient>, JsonDeserializer<Ingredient> {
    @Override
    public JsonElement serialize(Ingredient src, Type typeOfSrc, JsonSerializationContext context) {
        return src.toJson();
    }

    @Override
    public Ingredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json instanceof JsonPrimitive primitive) {
            return Ingredient.of(Registry.ITEM.get(new ResourceLocation(primitive.getAsString())));
        }
        return Ingredient.fromJson(json);
    }
}
