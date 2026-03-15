package com.agesmining.agesmining.datagen;

import com.agesmining.agesmining.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class ModLootTableProvider extends LootTableProvider {

    public ModLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)
        ));
    }

    static class ModBlockLoot extends BlockLootSubProvider {

        protected ModBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.MINE_SUPPORT_PILLAR.get());
            dropSelf(ModBlocks.MINE_SUPPORT_BEAM.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                ModBlocks.MINE_SUPPORT_PILLAR.get(),
                ModBlocks.MINE_SUPPORT_BEAM.get()
            );
        }
    }
}
