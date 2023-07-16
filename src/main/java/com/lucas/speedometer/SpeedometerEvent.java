package com.lucas.speedometer;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber(modid = Speedometer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpeedometerEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int TICK_PER_UPDATE = 10;

    private static Vector3d playerPosLastTick = null;
    private static double lastTickTime = -1;
    private static double speed = 0;
    private static int tickCount = 0;

    public static void calculateSpeed() {
        if (tickCount < TICK_PER_UPDATE) {
            tickCount += 1;
            return;
        }

        PlayerEntity player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        Vector3d playerPos = player.position();

        double currentTime = System.nanoTime();
        double elapsedTicks = (lastTickTime == -1) ? 0 : (currentTime - lastTickTime) / 1000000000;
        lastTickTime = currentTime;

        if (playerPosLastTick != null && elapsedTicks != 0) {
            double distance = getDistanceBetweenBlockPos(playerPos, playerPosLastTick);
            double speed = distance / elapsedTicks;
            SpeedometerEvent.speed = speed;
        }

        playerPosLastTick = playerPos;
        tickCount = 0;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        calculateSpeed();
    }

    public static double getDistanceBetweenBlockPos(Vector3d pos1, Vector3d pos2) {
        double dx = pos2.x() - pos1.x();
        double dz = pos2.z() - pos1.z();
        return MathHelper.sqrt(dx * dx + dz * dz);
    }

    @SubscribeEvent
    public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
        if (!Minecraft.getInstance().options.renderDebug) {
            return;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        event.getLeft().add("Speed: " + df.format(speed) + " b/s");
    }
}
