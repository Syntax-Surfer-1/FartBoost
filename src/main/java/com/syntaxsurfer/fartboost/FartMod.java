package com.syntaxsurfer.fartboost;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class FartMod implements ModInitializer {
    public static final String MOD_ID = "fartboost";
    public static SoundEvent FART_SOUND;

    @Override
    public void onInitialize() {
        Identifier fartId = Identifier.of(MOD_ID, "fart");
        FART_SOUND = SoundEvent.of(fartId);
        Registry.register(Registries.SOUND_EVENT, fartId, FART_SOUND);

        System.out.println("[FartBoost] Mod initialized!");
    }
}
