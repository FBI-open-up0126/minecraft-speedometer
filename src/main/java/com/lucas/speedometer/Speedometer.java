package com.lucas.speedometer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Speedometer.MOD_ID)
@Mod.EventBusSubscriber(modid = Speedometer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Speedometer {
    public static final String MOD_ID = "speedometer";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int TICK_PER_UPDATE = 10;

    private static Vector3d playerPosLastTick = null;
    private static double lastTickTime = -1;
    private static double speed = 0;
    private static int tickCount = 0;

    public Speedometer() {
        // Register the setup method for modloading
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(this::setup);
        // Register the enqueueIMC method for modloading
        eventBus.addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        eventBus.addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        eventBus.addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (tickCount < TICK_PER_UPDATE) {
                tickCount += 1;
                return;
            }

            PlayerEntity player = Minecraft.getInstance().player;

            if (player != null) {
                Vector3d playerPos = player.position();
                LOGGER.info("playerPos" + playerPos);

                double currentTime = System.nanoTime();
                double elapsedTicks = (lastTickTime == -1) ? 0 : (currentTime - lastTickTime) / 1000000000;
                LOGGER.info("elapsed tick:" + elapsedTicks);
                lastTickTime = currentTime;

                if (playerPosLastTick != null && elapsedTicks != 0) {
                    double distance = getDistanceBetweenBlockPos(playerPos, playerPosLastTick);
                    LOGGER.info("distance: " + distance);
                    double speed = 0;
                    speed = distance / elapsedTicks;
                    Speedometer.speed = speed;
                }

                playerPosLastTick = playerPos;
            }

            tickCount = 0;
        }
    }

    public static double getDistanceBetweenBlockPos(Vector3d pos1, Vector3d pos2) {
        double dx = pos2.x() - pos1.x();
//        double dy = pos2.y() - pos1.y();
        double dz = pos2.z() - pos1.z();
        return MathHelper.sqrt(dx * dx /* + dy * dy */ + dz * dz);
    }

    @SubscribeEvent
    public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
        if (Minecraft.getInstance().options.renderDebug) {
            DecimalFormat df = new DecimalFormat("#.##");

            event.getLeft().add("Speed: " + df.format(speed) + " b/s");
        }
    }


    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().options);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}",
                event.getIMCStream().map(m -> m.getMessageSupplier().get()).collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
