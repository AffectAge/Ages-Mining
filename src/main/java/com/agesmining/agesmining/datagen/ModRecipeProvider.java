package com.agesmining.agesmining.datagen;

import com.agesmining.agesmining.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {

        // Mine Support Pillar: 3 logs in a column → 3 pillars
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS,
                ModBlocks.MINE_SUPPORT_PILLAR.get(), 3)
            .pattern(" L ")
            .pattern(" L ")
            .pattern(" L ")
            .define('L', ItemTags.LOGS)
            .unlockedBy("has_logs", has(ItemTags.LOGS))
            .save(writer);

        // Mine Support Beam: row of 3 logs + 2 sticks → 4 beams
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS,
                ModBlocks.MINE_SUPPORT_BEAM.get(), 4)
            .pattern("LLL")
            .pattern("S S")
            .define('L', ItemTags.LOGS)
            .define('S', Items.STICK)
            .unlockedBy("has_logs", has(ItemTags.LOGS))
            .save(writer);
    }
}
