package com.syntaxsurfer.fartboost.client;

import com.syntaxsurfer.fartboost.FartMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.joml.Vector3f;

public class FartActions {
    private static final int HUNGER_COST = 2; // cost in hunger points
    private static final double BOOST_STRENGTH = 1.6; // horizontal boost
    private static final double MAX_VERTICAL = 0.9; // max upward force
    private static int cooldownTicks = 0;
    private static boolean cooldownEnabled = true;

    public static void setCooldownEnabled(boolean enabled) {
        cooldownEnabled = enabled;
    }

    public static void tryFart(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // No farting in spectator
        if (client.interactionManager != null &&
            client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) return;

        // Cooldown check
        if (cooldownEnabled && cooldownTicks > 0) {
            client.player.sendMessage(
                Text.literal("Cooldown: " + (cooldownTicks / 20) + "s")
                    .styled(style -> style.withColor(TextColor.fromRgb(0xFFFF55))),
                false
            );
            return;
        }
        if (!cooldownEnabled && cooldownTicks > 0) return;

        // Hunger check (creative bypass)
        if (!client.player.getAbilities().creativeMode &&
            client.player.getHungerManager().getFoodLevel() <= HUNGER_COST) {
            client.player.sendMessage(
                Text.literal("Too hungry to fart!")
                    .styled(style -> style.withColor(TextColor.fromRgb(0xFF5555))),
                false
            );
            return;
        }

        // Deduct hunger
        if (!client.player.getAbilities().creativeMode) {
            int newFood = Math.max(0, client.player.getHungerManager().getFoodLevel() - HUNGER_COST);
            client.player.getHungerManager().setFoodLevel(newFood);
        }

        // Boost movement
        var look = client.player.getRotationVector();
        double yBoost = Math.min(look.y * BOOST_STRENGTH, MAX_VERTICAL);
        double xBoost = look.x * BOOST_STRENGTH;
        double zBoost = look.z * BOOST_STRENGTH;
        client.player.addVelocity(xBoost, yBoost, zBoost);

        // Play the single fart sound
        client.world.playSound(
            client.player,
            client.player.getBlockPos(),
            FartMod.FART_SOUND,
            SoundCategory.PLAYERS,
            1.0F,
            MathHelper.nextFloat(client.world.random, 0.95F, 1.05F)
        );

        // Spawn green particles
        for (int i = 0; i < 90; i++) {
            double ox = (client.world.random.nextDouble() - 0.5) * 2.5;
            double oy = client.world.random.nextDouble() * 1.5;
            double oz = (client.world.random.nextDouble() - 0.5) * 2.5;
            client.world.addParticle(
                new DustParticleEffect(new Vector3f(0.2f, 1.0f, 0.2f), 3.0f),
                client.player.getX() + ox,
                client.player.getY() + 0.5 + oy,
                client.player.getZ() + oz,
                0, 0, 0
            );
        }

        // Apply cooldown
        cooldownTicks = cooldownEnabled ? 100 : 20;

        // Register cooldown tick
        ClientTickEvents.END_CLIENT_TICK.register(FartActions::tickCooldownOnce);
    }

    private static void tickCooldownOnce(MinecraftClient client) {
        if (cooldownTicks > 0) cooldownTicks--;
    }
}
