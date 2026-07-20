package com.cyoo.addon.modules;

import com.cyoo.addon.Addon;
import com.cyoo.addon.utils.HitboxChecker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

public class GrimAura extends Module {
    private double serverYaw;
    private double serverPitch;
    private boolean sprintToggled;
    private boolean justAttacked;

    private final Random random = new Random();
    private final SettingGroup sg = settings.getDefaultGroup();

    public GrimAura() {
        super(Addon.CATEGORY, "grim-aura", "Smooths combat rotations. Requires KillAura active.");
    }

    private final Setting<Double> smoothFactor = sg.add(
        new DoubleSetting.Builder()
            .name("smooth-factor")
            .description("Rotation smoothing (higher = snappier).")
            .sliderRange(0.3, 0.95).defaultValue(0.8)
            .build()
    );

    private final Setting<Double> jitter = sg.add(
        new DoubleSetting.Builder()
            .name("jitter")
            .description("Rotation jitter between attacks (degrees).")
            .sliderRange(0.0, 1.0).defaultValue(0.2)
            .build()
    );

    private final Setting<Boolean> sprintReset = sg.add(
        new BoolSetting.Builder()
            .name("sprint-reset")
            .description("Stop sprint before attack.")
            .defaultValue(true)
            .build()
    );

    @Override
    public void onActivate() {
        sprintToggled = false;
        justAttacked = false;
        if (mc != null && mc.player != null) {
            serverYaw = mc.player.getYaw();
            serverPitch = mc.player.getPitch();
        } else {
            serverYaw = 0.0;
            serverPitch = 0.0;
        }
    }

    @Override
    public void onDeactivate() {
        if (mc != null && mc.player != null && sprintToggled) {
            mc.player.setSprinting(true);
        }
        sprintToggled = false;
        justAttacked = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc == null || mc.player == null || mc.world == null) return;

        // Restore sprint after attack
        if (sprintToggled) {
            mc.player.setSprinting(true);
            sprintToggled = false;
        }

        // Skip rotation update right after attack — let vanilla handle it
        if (justAttacked) {
            justAttacked = false;
            return;
        }

        if (!Modules.get().isActive(KillAura.class)) return;

        Entity target = mc.targetedEntity;
        if (!(target instanceof LivingEntity)) return;

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetCenter = HitboxChecker.getTargetCenter(target);
        if (targetCenter == null) return;

        double x = targetCenter.x - eyePos.x;
        double y = targetCenter.y - eyePos.y;
        double z = targetCenter.z - eyePos.z;
        double horizontalDistance = Math.sqrt(x * x + z * z);

        double targetYaw = Math.toDegrees(Math.atan2(z, x)) - 90.0;
        double targetPitch = -Math.toDegrees(Math.atan2(y, horizontalDistance));

        // Smooth rotation — jitter only between attacks
        float jitterAmount = jitter.get().floatValue();
        double yawJitter = (random.nextDouble() - 0.5) * 2.0 * jitterAmount;
        double pitchJitter = (random.nextDouble() - 0.5) * 2.0 * jitterAmount;

        double factor = smoothFactor.get();
        serverYaw = serverYaw * (1.0 - factor) + (targetYaw + yawJitter) * factor;
        serverPitch = serverPitch * (1.0 - factor) + (targetPitch + pitchJitter) * factor;
        serverPitch = MathHelper.clamp(serverPitch, -90.0, 90.0);

        Rotations.rotate(serverYaw, serverPitch, 100);
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (mc == null || mc.player == null) return;
        if (!Modules.get().isActive(KillAura.class)) return;
        if (!(event.entity instanceof LivingEntity)) return;

        // Send EXACT rotation to target — no jitter, passes hitbox check
        double exactYaw = Rotations.getYaw(event.entity);
        double exactPitch = Rotations.getPitch(event.entity, Target.Body);
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.LookAndOnGround(
                (float) exactYaw, (float) exactPitch,
                mc.player.isOnGround(), mc.player.horizontalCollision
            )
        );

        // Sprint reset
        if (sprintReset.get() && mc.player.isSprinting()) {
            sprintToggled = true;
            mc.player.setSprinting(false);
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(
                    mc.player.isOnGround(), mc.player.horizontalCollision
                )
            );
        }

        justAttacked = true;
    }
}
