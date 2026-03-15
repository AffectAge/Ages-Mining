package com.agesmining.agesmining.util;

import com.agesmining.agesmining.AgesMining;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Datapack-driven support definitions, inspired by TFC's support data manager.
 * Files are loaded from: data/agesmining/supports/*.json
 */
public class SupportDataManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    public static final SupportDataManager INSTANCE = new SupportDataManager();

    private volatile List<SupportDefinition> supports = List.of();
    private volatile SupportRange maxRange = new SupportRange(0, 0, 0);

    private SupportDataManager() {
        super(GSON, "supports");
    }

    @Override
    protected void apply(java.util.Map<ResourceLocation, JsonElement> map,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        List<SupportDefinition> loaded = new ArrayList<>();
        int maxUp = 0;
        int maxDown = 0;
        int maxHorizontal = 0;

        for (var entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) continue;

            try {
                SupportDefinition definition = parseDefinition(id, element.getAsJsonObject());
                loaded.add(definition);
                maxUp = Math.max(maxUp, definition.supportUp());
                maxDown = Math.max(maxDown, definition.supportDown());
                maxHorizontal = Math.max(maxHorizontal, definition.supportHorizontal());
            } catch (Exception ex) {
                AgesMining.LOGGER.error("Failed to parse support definition {}: {}", id, ex.getMessage());
            }
        }

        // Safety fallback: if datapack provides no supports, mine beams still work.
        if (loaded.isEmpty()) {
            Set<Block> fallbackBlocks = new HashSet<>();
            fallbackBlocks.add(com.agesmining.agesmining.registry.ModBlocks.MINE_SUPPORT_BEAM.get());
            loaded.add(new SupportDefinition(
                new ResourceLocation(AgesMining.MOD_ID, "fallback_horizontal_support_beam"),
                fallbackBlocks,
                Set.of(),
                2, 2, 4
            ));
            maxUp = 2;
            maxDown = 2;
            maxHorizontal = 4;
        }

        supports = List.copyOf(loaded);
        maxRange = new SupportRange(maxUp, maxDown, maxHorizontal);
        AgesMining.LOGGER.info("Loaded {} support definition(s). Max range up/down/h: {}/{}/{}",
            supports.size(), maxRange.up(), maxRange.down(), maxRange.horizontal());
    }

    public SupportRange getSupportCheckRange() {
        return maxRange;
    }

    public boolean isSupported(BlockGetter world, BlockPos pos) {
        for (BlockPos supportPos : getMaximumSupportedAreaAround(pos, pos)) {
            SupportDefinition support = get(world.getBlockState(supportPos));
            if (support != null && support.canSupport(supportPos, pos)) {
                return true;
            }
        }
        return false;
    }

    public Set<BlockPos> findUnsupportedPositions(BlockGetter world, BlockPos from, BlockPos to) {
        Set<BlockPos> listSupported = new HashSet<>();
        Set<BlockPos> listUnsupported = new HashSet<>();

        int minX = Math.min(from.getX(), to.getX());
        int maxX = Math.max(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int maxY = Math.max(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxZ = Math.max(from.getZ(), to.getZ());

        for (BlockPos searchingPoint : getMaximumSupportedAreaAround(
            new BlockPos(minX, minY, minZ),
            new BlockPos(maxX, maxY, maxZ)
        )) {
            BlockPos immutableSearch = searchingPoint.immutable();
            if (!listSupported.contains(immutableSearch)) {
                listUnsupported.add(immutableSearch);
            }

            SupportDefinition support = get(world.getBlockState(immutableSearch));
            if (support != null) {
                for (BlockPos supported : support.getSupportedArea(immutableSearch)) {
                    BlockPos immutableSupported = supported.immutable();
                    listSupported.add(immutableSupported);
                    listUnsupported.remove(immutableSupported);
                }
            }
        }

        listUnsupported.removeIf(pos ->
            pos.getX() < minX || pos.getX() > maxX ||
                pos.getY() < minY || pos.getY() > maxY ||
                pos.getZ() < minZ || pos.getZ() > maxZ
        );
        return listUnsupported;
    }

    private Iterable<BlockPos> getMaximumSupportedAreaAround(BlockPos minPoint, BlockPos maxPoint) {
        SupportRange range = maxRange;
        return BlockPos.betweenClosed(
            minPoint.offset(-range.horizontal(), -range.down(), -range.horizontal()),
            maxPoint.offset(range.horizontal(), range.up(), range.horizontal())
        );
    }

    private SupportDefinition get(BlockState state) {
        for (SupportDefinition support : supports) {
            if (support.matches(state)) {
                return support;
            }
        }
        return null;
    }

    private SupportDefinition parseDefinition(ResourceLocation id, JsonObject json) {
        JsonElement ingredientElement = json.get("ingredient");
        if (ingredientElement == null) {
            throw new JsonSyntaxException("Missing 'ingredient' field");
        }
        Set<Block> blocks = new HashSet<>();
        Set<TagKey<Block>> tags = new HashSet<>();

        if (ingredientElement.isJsonArray()) {
            JsonArray array = ingredientElement.getAsJsonArray();
            for (JsonElement entry : array) {
                parseIngredientValue(entry.getAsString(), blocks, tags);
            }
        } else if (ingredientElement.isJsonPrimitive()) {
            parseIngredientValue(ingredientElement.getAsString(), blocks, tags);
        } else {
            throw new JsonSyntaxException("'ingredient' must be a string or string[]");
        }

        int supportUp = GsonHelper.getAsInt(json, "support_up", 0);
        int supportDown = GsonHelper.getAsInt(json, "support_down", 0);
        int supportHorizontal = GsonHelper.getAsInt(json, "support_horizontal", 0);
        if (supportUp < 0 || supportDown < 0 || supportHorizontal < 0) {
            throw new JsonSyntaxException("support_* values must be nonnegative");
        }

        return new SupportDefinition(id, blocks, tags, supportUp, supportDown, supportHorizontal);
    }

    private void parseIngredientValue(String raw, Set<Block> blocks, Set<TagKey<Block>> tags) {
        if (raw.startsWith("#")) {
            ResourceLocation tagId = new ResourceLocation(raw.substring(1));
            tags.add(TagKey.create(Registries.BLOCK, tagId));
            return;
        }

        ResourceLocation blockId = new ResourceLocation(raw);
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) {
            AgesMining.LOGGER.warn("Unknown support ingredient block id '{}'", raw);
            return;
        }
        blocks.add(block);
    }

    public record SupportRange(int up, int down, int horizontal) {}

    private record SupportDefinition(
        ResourceLocation id,
        Set<Block> blocks,
        Set<TagKey<Block>> tags,
        int supportUp,
        int supportDown,
        int supportHorizontal
    ) {
        public boolean matches(BlockState state) {
            if (blocks.contains(state.getBlock())) return true;
            for (TagKey<Block> tag : tags) {
                if (state.is(tag)) return true;
            }
            return false;
        }

        public boolean canSupport(BlockPos supportPos, BlockPos testPos) {
            BlockPos diff = supportPos.subtract(testPos);
            return Math.abs(diff.getX()) <= supportHorizontal &&
                -supportDown <= diff.getY() && diff.getY() <= supportUp &&
                Math.abs(diff.getZ()) <= supportHorizontal;
        }

        public Iterable<BlockPos> getSupportedArea(BlockPos center) {
            return BlockPos.betweenClosed(
                center.offset(-supportHorizontal, -supportDown, -supportHorizontal),
                center.offset(supportHorizontal, supportUp, supportHorizontal)
            );
        }
    }
}
