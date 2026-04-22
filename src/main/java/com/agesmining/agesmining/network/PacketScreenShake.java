package com.agesmining.agesmining.network;

import com.agesmining.agesmining.client.ScreenShakeHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → Client packet: triggers a screen shake effect on the client.
 */
public class PacketScreenShake {

    private final float intensity;

    public PacketScreenShake(float intensity) {
        this.intensity = intensity;
    }

    // ── Encode ───────────────────────────────────────────────────────────────
    public static void encode(PacketScreenShake msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.intensity);
    }

    // ── Decode ───────────────────────────────────────────────────────────────
    public static PacketScreenShake decode(FriendlyByteBuf buf) {
        return new PacketScreenShake(buf.readFloat());
    }

    // ── Handle ───────────────────────────────────────────────────────────────
    public static void handle(PacketScreenShake msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                () -> ScreenShakeHandler.triggerShake(msg.intensity)
            )
        );
        ctx.setPacketHandled(true);
    }
}
