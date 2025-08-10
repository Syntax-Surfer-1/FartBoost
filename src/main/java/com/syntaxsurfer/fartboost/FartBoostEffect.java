package com.syntaxsurfer.fartboost;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class FartBoostEffect extends MobEffect {

    public FartBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x98FB98); // Light green color
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // Apply Weakness
        entity.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                40, // 2 seconds
                0,
                false,
                true
        ));

        // Apply Nausea
        entity.addEffect(new MobEffectInstance(
                MobEffects.NAUSEA,
                40,
                0,
                false,
                true
        ));

        return true; // required for 1.21.x
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // tick every tick
    }
}
