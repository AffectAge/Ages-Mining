package com.agesmining.agesmining.compat.jade;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.block.MineSupportBeamBlock;
import com.agesmining.agesmining.registry.ModBlocks;
import com.agesmining.agesmining.registry.ModTags;
import com.agesmining.agesmining.util.SupportDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum AgesMiningJadeSupportProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(AgesMining.MOD_ID, "support_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (!isRelevant(state)) {
            return;
        }

        BlockPos pos = accessor.getPosition();
        BlockGetter level = accessor.getLevel();

        boolean supported = SupportDataManager.INSTANCE.isSupported(level, pos);
        String supportedValue = supported ? "yes" : "no";

        if (state.is(ModBlocks.MINE_SUPPORT_BEAM.get())) {
            int range = Math.max(1, SupportDataManager.INSTANCE.getSupportCheckRange().horizontal());
            String anchorValue = isAnchoredBeam(level, pos, state, range) ? "yes" : "no";
            tooltip.add(Component.translatable("jade.agesmining.anchor")
                .append(Component.literal(": "))
                .append(coloredYesNo(anchorValue)));
        }

        String riskKey;
        if (supported) {
            riskKey = "safe";
        } else if (isPotentialSupportArea(level, pos)) {
            riskKey = "forecast";
        } else {
            riskKey = "unstable";
        }

        tooltip.add(Component.translatable("jade.agesmining.supported")
            .append(Component.literal(": "))
            .append(coloredYesNo(supportedValue)));

        tooltip.add(Component.translatable("jade.agesmining.risk")
            .append(Component.literal(": "))
            .append(coloredRisk(riskKey)));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private static boolean isRelevant(BlockState state) {
        return state.is(ModBlocks.MINE_SUPPORT_BEAM.get())
            || state.is(ModBlocks.MINE_SUPPORT_PILLAR.get())
            || state.is(ModTags.Blocks.CAN_COLLAPSE)
            || state.is(ModTags.Blocks.CAN_TRIGGER_COLLAPSE);
    }

    private static boolean isPotentialSupportArea(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.MINE_SUPPORT_BEAM.get()) || state.is(ModBlocks.MINE_SUPPORT_PILLAR.get())) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            BlockState near = level.getBlockState(pos.relative(direction));
            if (near.is(ModBlocks.MINE_SUPPORT_BEAM.get()) || near.is(ModBlocks.MINE_SUPPORT_PILLAR.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnchoredBeam(BlockGetter world, BlockPos beamPos, BlockState beamState, int maxHorizontal) {
        if (!beamState.is(ModBlocks.MINE_SUPPORT_BEAM.get())) return false;

        Direction.Axis axis = beamState.getValue(MineSupportBeamBlock.AXIS);
        Direction negative = axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
        Direction positive = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        BlockPos negAnchor = findPillarAnchor(world, beamPos, negative, axis, maxHorizontal + 1);
        BlockPos posAnchor = findPillarAnchor(world, beamPos, positive, axis, maxHorizontal + 1);
        if (negAnchor == null || posAnchor == null) return false;

        return SupportDataManager.isPillarStable(world, negAnchor) && SupportDataManager.isPillarStable(world, posAnchor);
    }

    private static BlockPos findPillarAnchor(BlockGetter world, BlockPos start, Direction dir, Direction.Axis beamAxis, int maxSteps) {
        for (int i = 1; i <= maxSteps; i++) {
            BlockPos at = start.relative(dir, i);
            BlockState state = world.getBlockState(at);
            if (state.is(ModBlocks.MINE_SUPPORT_BEAM.get())) {
                if (state.getValue(MineSupportBeamBlock.AXIS) != beamAxis) {
                    return null;
                }
                continue;
            }
            if (state.is(ModBlocks.MINE_SUPPORT_PILLAR.get())) {
                return at.immutable();
            }
            return null;
        }
        return null;
    }

    private static Component coloredYesNo(String key) {
        ChatFormatting color = "yes".equals(key) ? ChatFormatting.GREEN : ChatFormatting.RED;
        return Component.translatable("jade.agesmining.value." + key).withStyle(color);
    }

    private static Component coloredRisk(String riskKey) {
        ChatFormatting color = switch (riskKey) {
            case "safe" -> ChatFormatting.GREEN;
            case "forecast" -> ChatFormatting.YELLOW;
            default -> ChatFormatting.RED;
        };
        return Component.translatable("jade.agesmining.risk." + riskKey).withStyle(color);
    }
}
