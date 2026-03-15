package com.agesmining.agesmining.client;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.registry.ModBlocks;
import com.agesmining.agesmining.registry.ModTags;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * When a player holds a Mine Support item, renders translucent green outlines
 * around all nearby support structures so they can plan placements.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = AgesMining.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SupportHighlightRenderer {

    private static final int SCAN_RADIUS = 10;
    private static final float[] COLOR_SUPPORT   = {0.2f, 1.0f, 0.3f, 0.35f}; // green
    private static final float[] COLOR_UNSUPPORTED = {1.0f, 0.3f, 0.2f, 0.20f}; // red (ceiling at risk)

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // Only render when holding a support item
        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();
        boolean holdingSupport =
            main.getItem() instanceof net.minecraft.world.item.BlockItem bi &&
                (bi.getBlock() == ModBlocks.MINE_SUPPORT_PILLAR.get() ||
                 bi.getBlock() == ModBlocks.MINE_SUPPORT_BEAM.get())
            ||
            off.getItem() instanceof net.minecraft.world.item.BlockItem bi2 &&
                (bi2.getBlock() == ModBlocks.MINE_SUPPORT_PILLAR.get() ||
                 bi2.getBlock() == ModBlocks.MINE_SUPPORT_BEAM.get());

        if (!holdingSupport) return;

        BlockPos center = player.blockPosition();
        List<BlockPos> supports = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
            center.offset( SCAN_RADIUS,  SCAN_RADIUS,  SCAN_RADIUS))) {

            BlockState state = mc.level.getBlockState(pos);
            if (state.is(ModBlocks.MINE_SUPPORT_PILLAR.get()) ||
                state.is(ModBlocks.MINE_SUPPORT_BEAM.get())) {
                supports.add(pos.immutable());
            }
        }

        if (supports.isEmpty()) return;

        // ── Render outlines ───────────────────────────────────────────────
        var poseStack = event.getPoseStack();
        var camPos = mc.gameRenderer.getMainCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(2.0f);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = poseStack.last().pose();

        for (BlockPos pos : supports) {
            drawAABBOutline(buf, mat, new AABB(pos).inflate(0.01),
                COLOR_SUPPORT[0], COLOR_SUPPORT[1], COLOR_SUPPORT[2], COLOR_SUPPORT[3]);
        }

        tess.end();
        poseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void drawAABBOutline(BufferBuilder buf, Matrix4f mat,
                                         AABB box, float r, float g, float b, float a) {
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        // Bottom face
        line(buf, mat, x0,y0,z0, x1,y0,z0, r,g,b,a);
        line(buf, mat, x1,y0,z0, x1,y0,z1, r,g,b,a);
        line(buf, mat, x1,y0,z1, x0,y0,z1, r,g,b,a);
        line(buf, mat, x0,y0,z1, x0,y0,z0, r,g,b,a);
        // Top face
        line(buf, mat, x0,y1,z0, x1,y1,z0, r,g,b,a);
        line(buf, mat, x1,y1,z0, x1,y1,z1, r,g,b,a);
        line(buf, mat, x1,y1,z1, x0,y1,z1, r,g,b,a);
        line(buf, mat, x0,y1,z1, x0,y1,z0, r,g,b,a);
        // Vertical edges
        line(buf, mat, x0,y0,z0, x0,y1,z0, r,g,b,a);
        line(buf, mat, x1,y0,z0, x1,y1,z0, r,g,b,a);
        line(buf, mat, x1,y0,z1, x1,y1,z1, r,g,b,a);
        line(buf, mat, x0,y0,z1, x0,y1,z1, r,g,b,a);
    }

    private static void line(BufferBuilder buf, Matrix4f mat,
                               float x0, float y0, float z0,
                               float x1, float y1, float z1,
                               float r, float g, float b, float a) {
        buf.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
    }
}
