package com.lucas.speedometer.events;


import com.lucas.speedometer.SpeedInfo;
import com.lucas.speedometer.Speedometer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Speedometer.MOD_ID, bus = Bus.FORGE)
public class SpeedometerEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String TICK_UPDATE_SPEED_KEY = Speedometer.MOD_ID + "_tick_per_update";

    public static final double DEFAULT_SECONDS_PER_UPDATE = 1;

    private static Vector3d playerPosLastTick = null;
    private static SpeedInfo speedInfo;
    private static Optional<Double> secondsPerUpdate = Optional.empty();
    private static boolean hasStartedCalculation = false;
    private static final Runnable runnable = SpeedometerEvent::calculateSpeed;
    private static ScheduledExecutorService executor = null;
    private static ScheduledFuture<?> timerFuture = null;

    public static void calculateSpeed() {
        PlayerEntity player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        Vector3d playerPos = player.position();

        if (playerPosLastTick != null) {
            speedInfo = SpeedInfo.calculateSpeedInfo(playerPos, playerPosLastTick, secondsPerUpdate.get());
        }

        playerPosLastTick = playerPos;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!secondsPerUpdate.isPresent()) {
            loadTickPerUpdate();
        } else if (!hasStartedCalculation) {
            startCalculation();
        }
    }

    private static void restartExecutor() {
        if (executor == null || timerFuture == null) {
            return;
        }

        timerFuture.cancel(true);
        timerFuture = executor.scheduleAtFixedRate(runnable, 0, (int)(secondsPerUpdate.get() * 1000), TimeUnit.MILLISECONDS);
    }

    public static void startCalculation() {
        executor = Executors.newScheduledThreadPool(1);
        timerFuture = executor.scheduleAtFixedRate(runnable, 0, (int)(secondsPerUpdate.get() * 1000), TimeUnit.MILLISECONDS);

        hasStartedCalculation = true;
    }

    @SubscribeEvent
    public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
        if (!Minecraft.getInstance().options.renderDebug) {
            return;
        }

        String info = String.format("hor spd: %.2f b/s, ver spd: %.2f b/s, spd: %.2f b/s", speedInfo.horizontalSpeed, speedInfo.verticalSpeed, speedInfo.speed);
        event.getLeft().add(info);
    }

    public static String getConfigFilePath() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        return gameDir.getAbsolutePath() + "\\config\\speedometer_config.txt";
    }

    public static void setSecondsPerUpdate(double secondsPerUpdate) {
        if (secondsPerUpdate <= 0) {
            LOGGER.warn("Tick per update cannot be less than or equal 0!");
            return;
        }

        File file = new File(getConfigFilePath());
        try {
            if (file.createNewFile()) {
                LOGGER.info("File created");
            } else {
                LOGGER.info("File already exist");
            }
        } catch (IOException exception) {
            LOGGER.error("FAILED TO CREATE CONFIG FILE!");
            exception.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(getConfigFilePath());
            myWriter.write(String.valueOf(secondsPerUpdate));
            myWriter.close();
            LOGGER.info("Successfully wrote to the file.");
        } catch (IOException e) {
            LOGGER.error("An error occurred.");
            e.printStackTrace();
        }

        SpeedometerEvent.secondsPerUpdate = Optional.of(secondsPerUpdate);
        restartExecutor();
    }

    public static Optional<Double> getSecondsPerUpdate() {
        return secondsPerUpdate;
    }

    public static void loadTickPerUpdate() {
        PlayerEntity player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        double savedTickPerUpdate = -1;
        try {
            File configFile = new File(getConfigFilePath());
            Scanner scanner = new Scanner(configFile);
            if (scanner.hasNextDouble()) {
                savedTickPerUpdate = scanner.nextDouble();
            }
            scanner.close();
        } catch (FileNotFoundException e) {
        }

        if (savedTickPerUpdate == -1) {
            secondsPerUpdate = Optional.of(DEFAULT_SECONDS_PER_UPDATE);
            return;
        }

        secondsPerUpdate = Optional.of(savedTickPerUpdate);
        LOGGER.info("Final seconds per update: " + secondsPerUpdate);
    }
}
