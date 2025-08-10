package com.syntaxsurfer.fartboost;

import net.fabricmc.api.ModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FartBoost implements ModInitializer {
	public static final String MOD_ID = "fartboost";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean cooldownEnabled = true;

	// Holder instead of plain MobEffect
	public static Holder<MobEffect> FART_POTION_EFFECT;

	@Override
	public void onInitialize() {
		// Register the custom Fart Potion effect using Holder
		FART_POTION_EFFECT = Registry.registerForHolder(
				BuiltInRegistries.MOB_EFFECT,
				ResourceLocation.fromNamespaceAndPath(MOD_ID, "fart_potion_effect"),
				new FartBoostEffect()
		);

		// Register commands
		FartBoostCommand.register();

		LOGGER.info("FartBoost initialized! Prepare for liftoff 💨");
	}

	public static Component greenMsg(String msg) {
		return Component.literal(msg).withStyle(ChatFormatting.GREEN);
	}

	public static Component yellowMsg(String msg) {
		return Component.literal(msg).withStyle(ChatFormatting.YELLOW);
	}

	public static Component redMsg(String msg) {
		return Component.literal(msg).withStyle(ChatFormatting.RED);
	}
}
