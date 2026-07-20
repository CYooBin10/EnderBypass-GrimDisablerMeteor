package com.cyoo.addon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class HitboxChecker {

    public static Vec3d getTargetCenter(Entity entity) {
        if (entity == null) return null;
        return new Vec3d(
            entity.getX(),
            entity.getY() + entity.getStandingEyeHeight() * 0.5,
            entity.getZ()
        );
    }

    public static double getDistanceToEntity(PlayerEntity player, Entity entity) {
        if (player == null || entity == null) return Double.POSITIVE_INFINITY;
        Vec3d targetCenter = getTargetCenter(entity);
        if (targetCenter == null) return Double.POSITIVE_INFINITY;
        return player.getEyePos().distanceTo(targetCenter);
    }

    public static boolean isEntityReachable(Entity entity, double maxReach) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || entity == null) return false;
        if (maxReach < 0.0) return false;
        return getDistanceToEntity(mc.player, entity) <= maxReach;
    }

    public static boolean hasLineOfSight(PlayerEntity player, Entity target) {
        if (player == null || target == null) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return false;

        Vec3d targetCenter = getTargetCenter(target);
        if (targetCenter == null) return false;

        Vec3d eyePos = player.getEyePos();

        BlockHitResult result = mc.world.raycast(new RaycastContext(
            eyePos,
            targetCenter,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            player
        ));

        if (result.getType() == HitResult.Type.MISS) return true;

        return eyePos.distanceTo(result.getPos())
            >= eyePos.distanceTo(targetCenter) - 0.05;
    }

    public static boolean canAttack(PlayerEntity player, Entity target, double maxReach) {
        if (player == null || target == null) return false;
        return getDistanceToEntity(player, target) <= maxReach
            && hasLineOfSight(player, target);
    }

    public static boolean canAttack(PlayerEntity player, Entity target) {
        if (player == null || target == null) return false;
        return canAttack(player, target, 3.0);
    }
}
