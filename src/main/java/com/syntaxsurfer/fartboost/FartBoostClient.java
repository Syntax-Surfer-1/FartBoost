package com.syntaxsurfer.fartboost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v2.ClientTickEvents;
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
import net.minecraft.network.chat.Component;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FartBoostClient implements ClientModInitializer {
    private static KeyMapping fartKey;
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static final Random random = new Random();
    private static final int COOLDOWN_TICKS = 100;
    private static final SoundEvent FART_SOUND = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(FartBoost.MOD_ID, "fart")
    );

    @Override
    public void onInitializeClient() {
        fartKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.fartboost.fart",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.fartboost"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;

            Player player = client.player;
            UUID id = player.getUUID();

            // Tick cooldown down
            cooldowns.computeIfPresent(id, (u, ticks) -> ticks > 0 ? ticks - 1 : null);

            while (fartKey.consumeClick()) {
                if (player.isSpectator()) return;

                // Cooldown check
                if (FartBoost.cooldownEnabled && cooldowns.containsKey(id)) {
                    int seconds = (cooldowns.get(id) + 19) / 20;
                    player.displayClientMessage(Component.literal("Cooldown: " + seconds + "s"), false);
                    return;
                }

                // Hunger check (except Creative)
                if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() < 2) {
                    player.displayClientMessage(Component.literal("Too hungry to fart!"), false);
                    return;
                }

                // Reduce hunger
                if (!player.getAbilities().instabuild) {
                    player.causeFoodExhaustion(2.0F);
                }

                // Apply velocity
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(
                        look.x * 1.6,
                        Math.min(look.y * 1.6, 0.9),
                        look.z * 1.6
                );
                player.hurtMarked = true;

                // Spawn particles
                // Spawn particles
                for (int i = 0; i < 80 + random.nextInt(21); i++) {
                    double dx = (random.nextDouble() - 0.5) * 0.5;
                    double dy = random.nextDouble() * 0.5;
                    double dz = (random.nextDouble() - 0.5) * 0.5;

                    client.level.addParticle(
                            DustParticleOptions.redstone(new Vector3f(0.2f, 1.0f, 0.2f), 1.0f),
                            player.getX(), player.getY(), player.getZ(),
                            dx, dy, dz
                    );
                }


                // Play sound
                float pitch = 0.95F + random.nextFloat() * 0.1F;
                client.level.playSound(
                        player,
                        player.getX(), player.getY(), player.getZ(),
                        FART_SOUND,
                        SoundSource.PLAYERS,
                        1.0F,
                        pitch
                );

                // Set cooldown
                if (FartBoost.cooldownEnabled) cooldowns.put(id, COOLDOWN_TICKS);
            }
        });
    }
}
