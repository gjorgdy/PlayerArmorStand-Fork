package com.danrus.pas.commands;

import com.danrus.pas.managers.PasManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;

//~ screen_render
public class PasCommandsRegistrar<S> {
    public void register(CommandDispatcher<S> dispatcher) {
        PasCommands.COMMANDS_NAMES.forEach(s -> {
            dispatcher.register(
                    literal(s).executes((a) -> {
                                Minecraft.getInstance().gui.getChat().addMessage(PasCommands.defaultCommand());
                                return 1;
                            })
                            .then(literal("reload_failed").executes(PasCommands::reloadFailedCommand))
                            .then(literal("reload")
                                    .then(literal("skin")
                                            .then(argument("name/skin", DataStoreKeyArgumentType.forSkin()).executes(PasCommands::reloadSingeSkinCommand)))

                                    .then(literal("cape")
                                            .then(argument("name/cape", DataStoreKeyArgumentType.forCape()).executes(PasCommands::reloadSingleCapeCommand)))
                            )
                            .then(literal("debug")
                                    .then(literal("drop_cache").executes(PasCommands::dropCacheCommand))
                            )
            );
        });
    }

    private LiteralArgumentBuilder<S> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public <T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}