package com.agesmining.agesmining.network;

import com.agesmining.agesmining.AgesMining;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(AgesMining.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(id++,
            PacketScreenShake.class,
            PacketScreenShake::encode,
            PacketScreenShake::decode,
            PacketScreenShake::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    /** Send a screen shake packet to a specific player. */
    public static void sendShakeTo(ServerPlayer player, float intensity) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketScreenShake(intensity));
    }

    /** Send a screen shake to all players within a given distance of a position. */
    public static void sendShakeToNear(net.minecraft.server.level.ServerLevel level,
                                        double x, double y, double z,
                                        double range, float intensity) {
        CHANNEL.send(PacketDistributor.NEAR.with(() ->
            new PacketDistributor.TargetPoint(x, y, z, range, level.dimension())),
            new PacketScreenShake(intensity));
    }
}
