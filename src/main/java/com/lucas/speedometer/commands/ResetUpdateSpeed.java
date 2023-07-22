package com.lucas.speedometer.commands;

import com.lucas.speedometer.events.SpeedometerEvent;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class ResetUpdateSpeed {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("speedometer")
            .then(Commands.literal("updatespeed")
            .then(Commands.literal("reset")
            .executes((commands) -> resetUpdateSpeed(commands.getSource())))));
    }

    private static int resetUpdateSpeed(CommandSource source) {
        SpeedometerEvent.setSecondsPerUpdate(SpeedometerEvent.DEFAULT_SECONDS_PER_UPDATE);
        source.sendSuccess(new StringTextComponent("Successfully reset seconds per update to " + SpeedometerEvent.DEFAULT_SECONDS_PER_UPDATE), true);

        return 1;
    }
}
