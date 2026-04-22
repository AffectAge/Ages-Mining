package com.agesmining.agesmining.registry;

import com.agesmining.agesmining.AgesMining;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {
        /** Blocks tagged here will never collapse and count as support anchors */
        public static final TagKey<Block> NON_COLLAPSIBLE = tag("non_collapsible");

        /** Blocks that provide zero structural integrity */
        public static final TagKey<Block> NON_STRUCTURAL = tag("non_structural");

        /** All mine support blocks (pillars + beams) */
        public static final TagKey<Block> MINE_SUPPORTS = tag("mine_supports");

        /** All vanilla ore blocks (mapped to ore stability config) */
        public static final TagKey<Block> ALL_ORES = tag("all_ores");

        /** Blocks that can trigger collapse checks when mined/chiseled */
        public static final TagKey<Block> CAN_TRIGGER_COLLAPSE = tag("can_trigger_collapse");

        /** Blocks that may be selected as a collapse start point */
        public static final TagKey<Block> CAN_START_COLLAPSE = tag("can_start_collapse");

        /** Blocks that are allowed to fall during an active collapse wave */
        public static final TagKey<Block> CAN_COLLAPSE = tag("can_collapse");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AgesMining.MOD_ID, name));
        }
    }
}
