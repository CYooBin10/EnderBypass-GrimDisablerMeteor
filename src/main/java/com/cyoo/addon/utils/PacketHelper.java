package com.cyoo.addon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PacketHelper {
    public static void sendUseTrident(MinecraftClient mc) {
        if (mc == null || mc.player == null) return;
        mc.player.networkHandler.sendPacket(
            new PlayerInteractItemC2SPacket(
                Hand.MAIN_HAND,
                0,
                mc.player.getYaw(),
                mc.player.getPitch()
            )
        );
    }

    public static void sendReleaseTrident(MinecraftClient mc) {
        if (mc == null || mc.player == null) return;
        mc.player.networkHandler.sendPacket(
            new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                BlockPos.ORIGIN,
                Direction.DOWN
            )
        );
    }

    public static void sendPosition(MinecraftClient mc, double x, double y, double z, boolean onGround) {
        if (mc == null || mc.player == null) return;
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.PositionAndOnGround(
                x, y, z, onGround, mc.player.horizontalCollision
            )
        );
    }

    public static void sendOnGround(MinecraftClient mc, boolean onGround) {
        if (mc == null || mc.player == null) return;
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.OnGroundOnly(
                onGround, mc.player.horizontalCollision
            )
        );
    }
}
