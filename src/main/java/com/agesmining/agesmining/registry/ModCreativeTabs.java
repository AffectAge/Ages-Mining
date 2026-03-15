package com.agesmining.agesmining.registry;

import com.agesmining.agesmining.AgesMining;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AgesMining.MOD_ID);

    public static final RegistryObject<CreativeModeTab> AGES_MINING_TAB =
        CREATIVE_MODE_TABS.register("ages_mining_tab", () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.agesmining.main"))
                .icon(() -> new ItemStack(ModItems.MINE_SUPPORT_PILLAR.get()))
                .displayItems((params, output) -> {
                    output.accept(ModItems.MINE_SUPPORT_PILLAR.get());
                    output.accept(ModItems.MINE_SUPPORT_BEAM.get());
                })
                .build()
        );

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
