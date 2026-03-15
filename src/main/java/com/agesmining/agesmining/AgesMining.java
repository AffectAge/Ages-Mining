package com.agesmining.agesmining;

import com.agesmining.agesmining.config.AgesMiningConfig;
import com.agesmining.agesmining.event.CaveInEventHandler;
import com.agesmining.agesmining.event.PlayerMiningHandler;
import com.agesmining.agesmining.network.NetworkHandler;
import com.agesmining.agesmining.registry.ModBlockEntities;
import com.agesmining.agesmining.registry.ModBlocks;
import com.agesmining.agesmining.registry.ModCreativeTabs;
import com.agesmining.agesmining.registry.ModItems;
import com.agesmining.agesmining.registry.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AgesMining.MOD_ID)
public class AgesMining {

    public static final String MOD_ID = "agesmining";
    public static final Logger LOGGER = LogManager.getLogger();

    public AgesMining() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AgesMiningConfig.SPEC, "agesmining-common.toml");

        MinecraftForge.EVENT_BUS.register(new CaveInEventHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerMiningHandler());
        // AgesMiningCommand auto-registered via @Mod.EventBusSubscriber
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.register();
            LOGGER.info("Ages Mining network registered.");
        });
        LOGGER.info("Ages Mining initialized. Mine carefully!");
    }
}
