package com.agesmining.agesmining.client;

import com.agesmining.agesmining.AgesMining;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Client-only setup. Runs only on physical/logical clients.
 */
@Mod.EventBusSubscriber(modid = AgesMining.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AgesMining.LOGGER.info("Ages Mining client setup complete.");
        // Screen shake and highlight renderers are registered via @Mod.EventBusSubscriber
        // on the FORGE bus automatically — nothing extra needed here.
    }
}
