package com.lucas.speedometer.commands;

import com.lucas.speedometer.events.SpeedometerEvent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class UpdateUpdateSpeedCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("speedometer")
            .then(Commands.literal("updatespeed")
            .then(Commands.literal("set")
            .then(Commands.argument("seconds_per_update", DoubleArgumentType.doubleArg(0.1))
            .executes((commands) -> updateUpdateSpeed(commands.getSource(), DoubleArgumentType.getDouble(commands, "seconds_per_update")))))));
    }

    private static int updateUpdateSpeed(CommandSource source, double secondsPerUpdate) throws CommandSyntaxException {
        SpeedometerEvent.setSecondsPerUpdate(secondsPerUpdate);
        source.sendSuccess(new StringTextComponent("Successfully set seconds per update to " + secondsPerUpdate), true);

        return 1;
    }
}
