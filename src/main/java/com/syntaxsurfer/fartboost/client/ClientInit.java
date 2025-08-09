package com.syntaxsurfer.fartboost.client;

import com.syntaxsurfer.fartboost.FartMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class ClientInit implements ClientModInitializer {
    private static final String CATEGORY = "key.categories.fartboost";
    public static KeyBinding FART_KEY;

    @Override
    public void onInitializeClient() {
        // Register keybinding (G key)
        FART_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fartboost.fart",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        ));

        // Key press handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (FART_KEY.wasPressed()) {
                FartActions.tryFart(client);
            }
        });

        // Command for enabling/disabling cooldown (works in singleplayer LAN worlds)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("fartcooldown")
                .then(CommandManager.literal("on").executes(ctx -> {
                    FartActions.setCooldownEnabled(true);
                    ctx.getSource().sendFeedback(
                        () -> Text.literal("Fart cooldown enabled").formatted(Formatting.GREEN),
                        false
                    );
                    return 1;
                }))
                .then(CommandManager.literal("off").executes(ctx -> {
                    FartActions.setCooldownEnabled(false);
                    ctx.getSource().sendFeedback(
                        () -> Text.literal("Fart cooldown disabled").formatted(Formatting.RED),
                        false
                    );
                    return 1;
                }))
            );
        });
    }
}
