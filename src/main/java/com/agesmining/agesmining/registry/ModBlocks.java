package com.agesmining.agesmining.registry;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.block.MineSupportBeamBlock;
import com.agesmining.agesmining.block.MineSupportPillarBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, AgesMining.MOD_ID);

    public static final RegistryObject<Block> MINE_SUPPORT_PILLAR =
        BLOCKS.register("mine_support_pillar", MineSupportPillarBlock::new);

    public static final RegistryObject<Block> MINE_SUPPORT_BEAM =
        BLOCKS.register("mine_support_beam", MineSupportBeamBlock::new);
}
