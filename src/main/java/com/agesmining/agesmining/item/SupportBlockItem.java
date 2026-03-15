package com.agesmining.agesmining.item;

import com.agesmining.agesmining.config.AgesMiningConfig;
import com.agesmining.agesmining.util.SupportDataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Custom BlockItem with informative tooltips about the support range
 * that adjusts dynamically based on the current config values.
 */
public class SupportBlockItem extends BlockItem {

    public enum SupportType { PILLAR, BEAM }

    private final SupportType type;

    public SupportBlockItem(Block block, Properties props, SupportType type) {
        super(block, props);
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (type == SupportType.PILLAR) {
            int range = AgesMiningConfig.INSTANCE.SUPPORT_PILLAR_RANGE.get();
            SupportDataManager.SupportRange support = SupportDataManager.INSTANCE.getSupportCheckRange();

            tooltip.add(Component.literal("Вертикальный охват: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("±" + range + " блоков")
                    .withStyle(ChatFormatting.AQUA)));

            tooltip.add(Component.literal("Поддерживает потолок по колонне на ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("h=" + support.horizontal() + ", up=" + support.up() + ", down=" + support.down())
                    .withStyle(ChatFormatting.GREEN)));

            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Совет: ставь от пола до потолка.")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        } else {
            SupportDataManager.SupportRange support = SupportDataManager.INSTANCE.getSupportCheckRange();

            tooltip.add(Component.literal("Горизонтальный охват: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("±" + support.horizontal() + " блоков")
                    .withStyle(ChatFormatting.AQUA)));

            tooltip.add(Component.literal("Расширяет опору от столбов до ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(support.horizontal() + " блоков")
                    .withStyle(ChatFormatting.GREEN)));

            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Совет: крепи к столбам по потолку.")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        // Show stability info in advanced tooltip (F3+H)
        if (flag.isAdvanced()) {
            tooltip.add(Component.literal("Тип: " + type.name())
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
