package com.agesmining.agesmining.registry;

import com.agesmining.agesmining.AgesMining;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Block entity registry — currently empty, reserved for future features
 * such as support structure durability tracking.
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AgesMining.MOD_ID);

    // Future: SupportPillarBlockEntity for durability, connection tracking, etc.
}
