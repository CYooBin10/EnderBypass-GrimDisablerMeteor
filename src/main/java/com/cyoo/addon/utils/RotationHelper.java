package com.cyoo.addon.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.Rotations;

import java.util.Random;

public class RotationHelper {
    private static final Random random = new Random();

    public static void jitterRotate(Entity target, double strength) {
        double yaw = Rotations.getYaw(target);
        double pitch = Rotations.getPitch(target, Target.Body);

        double jY = (random.nextFloat() - 0.5f) * 2.0 * strength;
        double jP = (random.nextFloat() - 0.5f) * 2.0 * strength;

        Rotations.rotate(yaw + jY, MathHelper.clamp(pitch + jP, -90, 90), 200);
    }
}
