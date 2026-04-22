package com.agesmining.agesmining.client;

import com.agesmining.agesmining.config.AgesMiningConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import static com.agesmining.agesmining.AgesMining.MOD_ID;

/**
 * Client-side camera shake effect triggered when a cave-in occurs nearby.
 * Uses Forge's CameraSetupEvent to offset camera rotation temporarily.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ScreenShakeHandler {

    // Current shake state
    private static float shakeIntensity = 0f;
    private static float shakeDecay = 0f;
    private static int shakeTicks = 0;
    private static final int SHAKE_DURATION_TICKS = 30;

    // Cached random offsets
    private static float offsetX = 0f;
    private static float offsetY = 0f;

    /**
     * Trigger a camera shake effect. Called from network packet handler.
     * @param intensity  max rotation offset in degrees (e.g. 3.0)
     */
    public static void triggerShake(float intensity) {
        if (!AgesMiningConfig.INSTANCE.ENABLE_SCREEN_SHAKE.get()) return;
        shakeIntensity = Math.max(shakeIntensity, intensity);
        shakeDecay = intensity / SHAKE_DURATION_TICKS;
        shakeTicks = SHAKE_DURATION_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shakeTicks <= 0) {
            shakeIntensity = 0f;
            offsetX = 0f;
            offsetY = 0f;
            return;
        }

        shakeTicks--;
        shakeIntensity = Math.max(0f, shakeIntensity - shakeDecay);

        // Generate smooth perlin-like offset using sine curves
        float t = (SHAKE_DURATION_TICKS - shakeTicks) * 0.5f;
        offsetX = (float)(Math.sin(t * 2.3) * shakeIntensity);
        offsetY = (float)(Math.cos(t * 3.1) * shakeIntensity * 0.5f);
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeTicks <= 0 || shakeIntensity <= 0f) return;
        if (!AgesMiningConfig.INSTANCE.ENABLE_SCREEN_SHAKE.get()) return;

        event.setYaw(event.getYaw() + offsetX);
        event.setPitch(event.getPitch() + offsetY);
    }
}
