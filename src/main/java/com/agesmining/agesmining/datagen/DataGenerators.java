package com.agesmining.agesmining.datagen;

import com.agesmining.agesmining.AgesMining;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AgesMining.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var helper = event.getExistingFileHelper();

        // Server data
        gen.addProvider(event.includeServer(), new ModRecipeProvider(output));
        gen.addProvider(event.includeServer(), new ModBlockTagsProvider(output, lookupProvider, helper));
        gen.addProvider(event.includeServer(), new ModLootTableProvider(output));

        // Client assets
        gen.addProvider(event.includeClient(), new ModBlockStateProvider(output, helper));
        gen.addProvider(event.includeClient(), new ModItemModelProvider(output, helper));
    }
}
