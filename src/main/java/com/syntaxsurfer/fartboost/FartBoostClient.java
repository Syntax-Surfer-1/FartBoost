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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class FartBoostClient implements ClientModInitializer {
    private static KeyBinding fartKey;
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static final Random random = new Random();
    private static final int COOLDOWN_TICKS = 100;
    private static final SoundEvent FART_SOUND = SoundEvent.of(new Identifier(FartBoost.MOD_ID, "fart"));

    @Override
    public void onInitializeClient() {
        fartKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fartboost.fart",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.fartboost"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            PlayerEntity player = client.player;
            UUID id = player.getUuid();

            // Tick cooldown down
            cooldowns.computeIfPresent(id, (u, ticks) -> ticks > 0 ? ticks - 1 : null);

            while (fartKey.wasPressed()) {
                if (player.isSpectator()) return;

                // Cooldown check
                if (FartBoost.cooldownEnabled && cooldowns.containsKey(id)) {
                    int seconds = (cooldowns.get(id) + 19) / 20;
                    player.sendMessage(FartBoost.yellowMsg("Cooldown: " + seconds + "s"), true);
                    return;
                }

                // Hunger check (except Creative)
                if (!player.isCreative() && player.getHungerManager().getFoodLevel() < 2) {
                    player.sendMessage(FartBoost.redMsg("Too hungry to fart!"), true);
                    return;
                }

                // Reduce hunger
                if (!player.isCreative()) {
                    player.getHungerManager().addExhaustion(2.0F);
                }

                // Apply velocity
                Vec3d look = player.getRotationVec(1.0F);
                player.addVelocity(look.x * 1.6, Math.min(look.y * 1.6, 0.9), look.z * 1.6);
                player.velocityModified = true;

                // Spawn particles
                for (int i = 0; i < 80 + random.nextInt(21); i++) {
                    double dx = (random.nextDouble() - 0.5) * 0.5;
                    double dy = random.nextDouble() * 0.5;
                    double dz = (random.nextDouble() - 0.5) * 0.5;
                    client.world.addParticle(new DustParticleEffect(0.2f, 1.0f, 0.2f, 1.0f),
                            player.getX(), player.getY(), player.getZ(),
                            dx, dy, dz);
                }

                // Play sound
                client.world.playSound(player, player.getX(), player.getY(), player.getZ(),
                        FART_SOUND, SoundCategory.PLAYERS,
                        1.0F, MathHelper.nextFloat(random, 0.95F, 1.05F));

                // Set cooldown
                if (FartBoost.cooldownEnabled) cooldowns.put(id, COOLDOWN_TICKS);
            }
        });
    }
}
