package com.agesmining.agesmining.datagen;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, AgesMining.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerPillar();
        registerBeam();
    }

    private void registerPillar() {
        ModelFile model = models().getExistingFile(
            new ResourceLocation(AgesMining.MOD_ID, "block/mine_support_pillar"));

        getVariantBuilder(ModBlocks.MINE_SUPPORT_PILLAR.get())
            .forAllStates(state -> {
                var axis = state.getValue(BlockStateProperties.AXIS);
                int x = 0, z = 0;
                switch (axis) {
                    case X -> { x = 90; z = 90; }
                    case Z -> x = 90;
                    default -> {} // Y — upright
                }
                return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(x)
                    .rotationY(z)
                    .build();
            });
    }

    private void registerBeam() {
        ModelFile model = models().getExistingFile(
            new ResourceLocation(AgesMining.MOD_ID, "block/mine_support_beam"));

        getVariantBuilder(ModBlocks.MINE_SUPPORT_BEAM.get())
            .forAllStates(state -> {
                var axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                int y = axis == net.minecraft.core.Direction.Axis.Z ? 90 : 0;
                return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(y)
                    .build();
            });
    }
}
