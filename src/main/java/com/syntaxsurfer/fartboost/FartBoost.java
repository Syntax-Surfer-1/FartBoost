package com.syntaxsurfer.fartboost;

import net.fabricmc.api.ModInitializer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

	public static Text greenMsg(String msg) {
		return Text.literal(msg).formatted(Formatting.GREEN);
	}

	public static Text yellowMsg(String msg) {
		return Text.literal(msg).formatted(Formatting.YELLOW);
	}

	public static Text redMsg(String msg) {
		return Text.literal(msg).formatted(Formatting.RED);
	}
}
