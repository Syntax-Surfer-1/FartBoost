package com.syntaxsurfer.fartboost;

import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FartBoost implements ModInitializer {
	public static final String MOD_ID = "fartboost";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean cooldownEnabled = true;

	@Override
	public void onInitialize() {
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
