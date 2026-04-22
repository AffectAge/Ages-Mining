package com.agesmining.agesmining.client;

import com.agesmining.agesmining.AgesMining;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Client-only setup. Runs only on physical/logical clients.
 */
@Mod.EventBusSubscriber(modid = AgesMining.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AgesMining.LOGGER.info("Ages Mining client setup complete.");
        // Screen shake renderer is registered via @Mod.EventBusSubscriber on the FORGE bus.
    }
}
