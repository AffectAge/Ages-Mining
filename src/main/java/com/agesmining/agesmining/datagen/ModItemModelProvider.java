package com.agesmining.agesmining.datagen;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, AgesMining.MOD_ID, helper);
    }

    @Override
    protected void registerModels() {
        // Item models simply inherit from their block models
        withExistingParent(
            ModBlocks.MINE_SUPPORT_PILLAR.get().getDescriptionId()
                .replace("block.", ""),
            ResourceLocation.fromNamespaceAndPath(AgesMining.MOD_ID, "block/mine_support_pillar")
        );

        withExistingParent(
            ModBlocks.MINE_SUPPORT_BEAM.get().getDescriptionId()
                .replace("block.", ""),
            ResourceLocation.fromNamespaceAndPath(AgesMining.MOD_ID, "block/mine_support_beam")
        );
    }
}
