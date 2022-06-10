package com.matyrobbrt.dataobjects.gson.object;

import com.matyrobbrt.dataobjects.gson.ObjectReference;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import javax.annotation.Nullable;

public class JsonEffect {
    public final ObjectReference<MobEffect> effect;
    public int duration;
    public int amplifier = 0;
    public boolean ambient = false;
    public boolean noCounter = true;
    public boolean visible = true;
    public boolean showIcon = true;
    @Nullable
    public JsonEffect hiddenEffect;

    public JsonEffect(ObjectReference<MobEffect> effect) {
        this.effect = effect;
    }

    public MobEffectInstance build() {
        final var instance = new MobEffectInstance(effect.get(), duration, amplifier,
                ambient, visible, showIcon, hiddenEffect == null ? null : hiddenEffect.build(), effect.get().createFactorData());
        if (noCounter)
            instance.setNoCounter(true);
        return instance;
    }
}
