package com.syntaxsurfer.fartboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FartBoostClient implements ClientModInitializer {
	private static KeyMapping fartKey;
	private static final Map<UUID, Integer> cooldowns = new HashMap<>();
	private static final Random random = new Random();
	private static final int COOLDOWN_TICKS = 100; // 5s
	private static final SoundEvent FART_SOUND = SoundEvent.createVariableRangeEvent(
			ResourceLocation.fromNamespaceAndPath(FartBoost.MOD_ID, "fart")
	);

	private static long lastFartTime = 0;
	private static long lastPotionFart = 0;

	@Override
	public void onInitializeClient() {
		fartKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.fartboost.fart",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"category.fartboost"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null) return;
			Player player = client.player;
			UUID id = player.getUUID();
			long currentTick = client.level.getGameTime();

			// Tick cooldown
			cooldowns.computeIfPresent(id, (u, ticks) -> ticks > 0 ? ticks - 1 : null);

			// Show cooldown
			if (FartBoost.cooldownEnabled && cooldowns.containsKey(id)) {
				int seconds = (cooldowns.get(id) + 19) / 20;
				player.displayClientMessage(Component.literal("§eCooldown: " + seconds + "s"), true);
			}

			// Auto fart if potion effect active
			if (player.hasEffect(FartBoost.FART_POTION_EFFECT)) {
				if (currentTick - lastPotionFart >= 30) { // 1.5s
					doFart(player, false, true);
					lastPotionFart = currentTick;
				}
			}

			while (fartKey.consumeClick()) {
				if (player.isSpectator()) return;

				boolean superFart = player.isShiftKeyDown();
				int hungerCost = superFart ? 5 : 2;

				// Cooldown logic
				if (FartBoost.cooldownEnabled) {
					if (cooldowns.containsKey(id)) return;
				} else {
					if (currentTick - lastFartTime < 10) return;
					lastFartTime = currentTick;
				}

				// Hunger check
				if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() < hungerCost) {
					player.displayClientMessage(Component.literal("§cToo hungry to fart!"), true);
					return;
				}

				doFart(player, superFart, false);

				// Start cooldown
				if (FartBoost.cooldownEnabled) cooldowns.put(id, COOLDOWN_TICKS);
			}
		});
	}

	private void doFart(Player player, boolean superFart, boolean fromPotion) {
		if (!player.getAbilities().instabuild) {
			player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - (superFart ? 5 : 2));
		}

		// Sound
		float pitch = superFart ? 0.8F : 0.95F + random.nextFloat() * 0.1F;
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
				FART_SOUND, SoundSource.PLAYERS, superFart ? 2.0F : 1.0F, pitch);

		// Boost
		Vec3 look = player.getLookAngle();
		double boostMult = superFart ? 3.0 : 1.6;
		player.setDeltaMovement(look.x * boostMult,
				Math.min(look.y * boostMult, 1.2),
				look.z * boostMult);
		player.hurtMarked = true;

		// Particles
		spawnFartParticles(player, superFart ? 300 : 150, superFart ? 20.0f : 15.0f);

		// Super fart extras
		if (superFart && !fromPotion) {
			applySuperFartEffects(player);
		}

		// Potion self effects
		if (fromPotion) {
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
			player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
		}
	}

	private void spawnFartParticles(Player player, int amount, float size) {
		int r = (int) (0.2f * 255);
		int g = (int) (1.0f * 255);
		int b = (int) (0.2f * 255);
		int color = (r << 16) | (g << 8) | b;

		for (int i = 0; i < amount; i++) {
			double dx = (random.nextDouble() - 0.5) * 1.5;
			double dy = random.nextDouble() * 1.0;
			double dz = (random.nextDouble() - 0.5) * 1.5;

			player.level().addParticle(
					new DustParticleOptions(color, size),
					player.getX(), player.getY(), player.getZ(),
					dx, dy, dz
			);
		}
	}

	private void applySuperFartEffects(Player player) {
		// Apply Weakness + Nausea to mobs in 5 block radius
		List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(5),
				e -> e != player);
		for (LivingEntity entity : entities) {
			entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
			entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
			Vec3 knockback = entity.position().subtract(player.position()).normalize().scale(1.5);
			entity.push(knockback.x, 0.4, knockback.z);
		}

		// Extinguish candles/campfires nearby
		BlockPos.betweenClosedStream(player.blockPosition().offset(-5, -2, -5),
				player.blockPosition().offset(5, 2, 5)).forEach(pos -> {
			BlockState state = player.level().getBlockState(pos);
			if (state.getBlock() instanceof CandleBlock && state.getValue(CandleBlock.LIT)) {
				player.level().setBlock(pos, state.setValue(CandleBlock.LIT, false), 3);
			}
			if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
				player.level().setBlock(pos, state.setValue(CampfireBlock.LIT, false), 3);
			}
		});
	}
}
