package com.mcf.davidee.msc;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveHandler;

import com.mcf.davidee.msc.config.SpawnConfiguration;
import com.mcf.davidee.msc.forge.CommonProxy;
import com.mcf.davidee.msc.forge.SpawnFreqTicker;
import com.mcf.davidee.msc.network.PacketPipeline;
import com.mcf.davidee.msc.reflect.BiomeClassLoader;
import com.mcf.davidee.msc.spawning.MobHelper;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod(modid = "MSC2", name = "Mob Spawn Controls 2", dependencies = "after:*", version = MobSpawnControls.VERSION)
public class MobSpawnControls {

    public static final String VERSION = Tags.VERSION;
    public static final PacketPipeline DISPATCHER = new PacketPipeline();

    @SidedProxy(
        clientSide = "com.mcf.davidee.msc.forge.ClientProxy",
        serverSide = "com.mcf.davidee.msc.forge.CommonProxy")
    public static CommonProxy proxy;
    @Instance("MSC2")
    public static MobSpawnControls instance;

    private static FileHandler logHandler = null;
    private static Logger logger = Logger.getLogger("MobSpawnControls");

    public static Logger getLogger() {
        return logger;
    }

    private SpawnConfiguration config, defaultConfig;
    private SpawnFreqTicker ticker;

    @EventHandler
    public void load(FMLInitializationEvent event) {
        logger.setLevel(Level.ALL);

        try {
            File logfile = new File(proxy.getMinecraftDirectory(), "MobSpawnControls.log");
            if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite() && logHandler == null) {
                logHandler = new FileHandler(logfile.getPath());
                logHandler.setFormatter(new MSCLogFormatter());
                logger.addHandler(logHandler);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Mob Spawn Controls initializing...");

        ticker = new SpawnFreqTicker();
        DISPATCHER.initialize();
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent event) {
        BiomeClassLoader.loadBiomeClasses();
        logger.info("Generating default creature type map...");
        MobHelper.populateDefaultMap();
        logger.info("Mapping biomes...");
        BiomeNameHelper.initBiomeMap();
        logger.info("Creating default spawn configuration...");
        defaultConfig = new SpawnConfiguration(
            new File(new File(proxy.getMinecraftDirectory(), "config"), "default_msc"));
        defaultConfig.load();
        defaultConfig.save();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        ticker.tick();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        WorldServer world = server.worldServerForDimension(0);
        if (world != null && world.getSaveHandler() instanceof SaveHandler) {
            logger.info(
                "Creating spawn configuration for World \"" + world.getSaveHandler()
                    .getWorldDirectoryName() + "\"");
            config = new SpawnConfiguration(
                new File(((SaveHandler) world.getSaveHandler()).getWorldDirectory(), "msc"),
                defaultConfig);
            config.load();
            config.save();
        }
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        logger.info("Unloading spawn configuration");
        MSCLogFormatter.log.clear();
        config = null;
    }

    public SpawnConfiguration getConfigNoThrow() {
        return config;
    }

    public SpawnConfiguration getConfig() throws RuntimeException {
        if (config != null) return config;
        throw new RuntimeException("MSC: Null Spawn Config");
    }

}
