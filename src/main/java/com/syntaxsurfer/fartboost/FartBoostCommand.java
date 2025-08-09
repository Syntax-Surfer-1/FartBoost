package com.syntaxsurfer.fartboost;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class FartBoostCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fartboost")
                .then(Commands.literal("toggleCooldown")
                        .executes(ctx -> {
                            FartBoost.cooldownEnabled = !FartBoost.cooldownEnabled;
                            ctx.getSource().sendSuccess(() ->
                                    FartBoost.greenMsg("Cooldown is now " +
                                            (FartBoost.cooldownEnabled ? "ON" : "OFF")), false);
                            return 1;
                        })
                )
        );
    }
}
