package com.lucas.speedometer.events;

import com.lucas.speedometer.Speedometer;
import com.lucas.speedometer.commands.ResetUpdateSpeed;
import com.lucas.speedometer.commands.UpdateUpdateSpeedCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Speedometer.MOD_ID, bus = Bus.FORGE)
public class ModEvent {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        UpdateUpdateSpeedCommand.register(event.getDispatcher());
        ResetUpdateSpeed.register(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
