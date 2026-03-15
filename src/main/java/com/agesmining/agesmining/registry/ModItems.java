package com.agesmining.agesmining.registry;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.item.SupportBlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, AgesMining.MOD_ID);

    public static final RegistryObject<Item> MINE_SUPPORT_PILLAR =
        ITEMS.register("mine_support_pillar",
            () -> new SupportBlockItem(
                ModBlocks.MINE_SUPPORT_PILLAR.get(),
                new Item.Properties(),
                SupportBlockItem.SupportType.PILLAR));

    public static final RegistryObject<Item> MINE_SUPPORT_BEAM =
        ITEMS.register("mine_support_beam",
            () -> new SupportBlockItem(
                ModBlocks.MINE_SUPPORT_BEAM.get(),
                new Item.Properties(),
                SupportBlockItem.SupportType.BEAM));
}
