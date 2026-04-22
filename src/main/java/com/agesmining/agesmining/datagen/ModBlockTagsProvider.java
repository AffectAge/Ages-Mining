package com.agesmining.agesmining.datagen;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.registry.ModBlocks;
import com.agesmining.agesmining.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output,
                                 CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 ExistingFileHelper helper) {
        super(output, lookupProvider, AgesMining.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        // Mine supports tag
        tag(ModTags.Blocks.MINE_SUPPORTS)
            .add(ModBlocks.MINE_SUPPORT_PILLAR.get())
            .add(ModBlocks.MINE_SUPPORT_BEAM.get());

        // Non-collapsible tag
        tag(ModTags.Blocks.NON_COLLAPSIBLE)
            .add(Blocks.BEDROCK)
            .add(Blocks.OBSIDIAN)
            .add(Blocks.CRYING_OBSIDIAN)
            .add(Blocks.REINFORCED_DEEPSLATE)
            .add(Blocks.END_STONE)
            .add(Blocks.END_STONE_BRICKS)
            .add(ModBlocks.MINE_SUPPORT_PILLAR.get())
            .add(ModBlocks.MINE_SUPPORT_BEAM.get());

        // Non-structural tag
        tag(ModTags.Blocks.NON_STRUCTURAL)
            .add(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR)
            .add(Blocks.WATER, Blocks.LAVA)
            .add(Blocks.TORCH, Blocks.WALL_TORCH)
            .add(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH)
            .add(Blocks.LADDER, Blocks.VINE, Blocks.GLOW_LICHEN, Blocks.COBWEB)
            .add(Blocks.GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.DEAD_BUSH)
            .add(Blocks.SNOW)
            .addTag(BlockTags.FLOWERS)
            .addTag(BlockTags.SAPLINGS);

        // All ores tag
        tag(ModTags.Blocks.ALL_ORES)
            .addTag(BlockTags.COAL_ORES)
            .addTag(BlockTags.IRON_ORES)
            .addTag(BlockTags.GOLD_ORES)
            .addTag(BlockTags.DIAMOND_ORES)
            .addTag(BlockTags.EMERALD_ORES)
            .addTag(BlockTags.LAPIS_ORES)
            .addTag(BlockTags.REDSTONE_ORES)
            .addTag(BlockTags.COPPER_ORES)
            .add(Blocks.ANCIENT_DEBRIS)
            .add(Blocks.NETHER_QUARTZ_ORE)
            .add(Blocks.NETHER_GOLD_ORE);

        // TFC-like collapse tags
        tag(ModTags.Blocks.CAN_TRIGGER_COLLAPSE)
            .add(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE)
            .addTag(ModTags.Blocks.ALL_ORES);

        tag(ModTags.Blocks.CAN_START_COLLAPSE)
            .add(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE)
            .addTag(ModTags.Blocks.ALL_ORES);

        tag(ModTags.Blocks.CAN_COLLAPSE)
            .add(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE)
            .add(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, Blocks.TUFF)
            .addTag(ModTags.Blocks.ALL_ORES);

        // Mineable with axe (support structures)
        tag(BlockTags.MINEABLE_WITH_AXE)
            .add(ModBlocks.MINE_SUPPORT_PILLAR.get())
            .add(ModBlocks.MINE_SUPPORT_BEAM.get());
    }
}
