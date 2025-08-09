package com.syntaxsurfer.fartboost;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class FartBoostCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("fartboost")
                .then(CommandManager.literal("toggleCooldown")
                        .executes(ctx -> {
                            FartBoost.cooldownEnabled = !FartBoost.cooldownEnabled;
                            ctx.getSource().sendFeedback(() ->
                                    FartBoost.greenMsg("Cooldown is now " +
                                            (FartBoost.cooldownEnabled ? "ON" : "OFF")), false);
                            return 1;
                        })
                )
        );
    }
}
