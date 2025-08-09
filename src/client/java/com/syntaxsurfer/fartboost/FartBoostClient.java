package com.syntaxsurfer.fartboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FartBoostClient implements ClientModInitializer {
	private static KeyMapping fartKey;
	private static final Map<UUID, Integer> cooldowns = new HashMap<>();
	private static final Random random = new Random();
	private static final int COOLDOWN_TICKS = 100; // 5 seconds
	private static final SoundEvent FART_SOUND = SoundEvent.createVariableRangeEvent(
			ResourceLocation.fromNamespaceAndPath(FartBoost.MOD_ID, "fart")
	);

	// New: last fart tick for no-cooldown mode
	private static long lastFartTime = 0;

	@Override
	public void onInitializeClient() {
		fartKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.fartboost.fart",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R, // Changed from G to R
				"category.fartboost"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) return;
			Player player = client.player;
			UUID id = player.getUUID();

			// Tick cooldown down
			cooldowns.computeIfPresent(id, (u, ticks) -> ticks > 0 ? ticks - 1 : null);

			// Show cooldown in action bar
			if (FartBoost.cooldownEnabled && cooldowns.containsKey(id)) {
				int seconds = (cooldowns.get(id) + 19) / 20;
				player.displayClientMessage(Component.literal("§eCooldown: " + seconds + "s"), true);
			}

			while (fartKey.consumeClick()) {
				if (player.isSpectator()) return;

				long currentTick = client.level.getGameTime();

				// Cooldown check
				if (FartBoost.cooldownEnabled) {
					if (cooldowns.containsKey(id)) return;
				} else {
					// No cooldown mode: ensure 1s gap
					if (currentTick - lastFartTime < 10) return;
					lastFartTime = currentTick;
				}

				// Hunger check
				if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() < 2) {
					player.displayClientMessage(Component.literal("§cToo hungry to fart!"), true);
					return;
				}

				// Reduce hunger
				if (!player.getAbilities().instabuild) {
					player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 2);
				}

				// Play sound instantly
				float pitch = 0.95F + random.nextFloat() * 0.1F;
				client.level.playSound(
						player,
						player.getX(), player.getY(), player.getZ(),
						FART_SOUND,
						SoundSource.PLAYERS,
						1.0F,
						pitch
				);

				// Apply boost
				Vec3 look = player.getLookAngle();
				player.setDeltaMovement(
						look.x * 1.6,
						Math.min(look.y * 1.6, 0.9),
						look.z * 1.6
				);
				player.hurtMarked = true;

				// Bigger fart particles with larger radius
				int r = (int)(0.2f * 255);
				int g = (int)(1.0f * 255);
				int b = (int)(0.2f * 255);
				int color = (r << 16) | (g << 8) | b;

				for (int i = 0; i < 150 + random.nextInt(51); i++) {
					double dx = (random.nextDouble() - 0.5) * 1.5;
					double dy = random.nextDouble() * 1.0;
					double dz = (random.nextDouble() - 0.5) * 1.5;

					client.level.addParticle(
							new DustParticleOptions(color, 6.0f), // radius increased from 1.0f → 3.0f
							player.getX(), player.getY(), player.getZ(),
							dx, dy, dz
					);
				}

				// Start cooldown if enabled
				if (FartBoost.cooldownEnabled) cooldowns.put(id, COOLDOWN_TICKS);
			}
		});
	}
}
