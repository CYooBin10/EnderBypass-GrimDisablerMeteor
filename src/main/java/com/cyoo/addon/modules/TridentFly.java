package com.cyoo.addon.modules;

import com.cyoo.addon.Addon;
import net.minecraft.entity.MovementType;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;

public class TridentFly extends Module {
    private int currentTick;
    private int airTicks;

    private final SettingGroup sg = settings.getDefaultGroup();

    public TridentFly() {
        super(Addon.CATEGORY, "trident-fly", "Enhances Riptide trident flight with smooth physics.");
    }

    private final Setting<Integer> delay = sg.add(
        new IntSetting.Builder()
            .name("delay")
            .description("Ticks between boosts.")
            .sliderRange(0, 10).defaultValue(0)
            .build()
    );

    private final Setting<Double> speed = sg.add(
        new DoubleSetting.Builder()
            .name("speed")
            .description("Horizontal speed multiplier.")
            .sliderRange(0.5, 3.0).defaultValue(1.0)
            .build()
    );

    private final Setting<Double> verticalSpeed = sg.add(
        new DoubleSetting.Builder()
            .name("vertical-speed")
            .description("Vertical boost strength.")
            .sliderRange(0.1, 2.0).defaultValue(0.5)
            .build()
    );

    @Override
    public void onActivate() {
        currentTick = delay.get();
        airTicks = 0;
    }

    @Override
    public void onDeactivate() {
        currentTick = 0;
        airTicks = 0;
    }

    private boolean isNearGround() {
        if (mc == null || mc.world == null || mc.player == null) return false;
        BlockPos below = mc.player.getBlockPos().down(2);
        return mc.world.getBlockState(below).isSolidBlock(mc.world, below);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc == null || mc.player == null || mc.world == null) return;
        if (InvUtils.findInHotbar(Items.TRIDENT).slot() == -1) return;

        boolean nearGround = mc.player.isOnGround() || isNearGround();

        if (nearGround) {
            airTicks = 0;
        } else {
            airTicks++;
        }

        // Vertical sync: emit on-ground packet when near solid ground after flight
        if (!mc.player.isOnGround() && isNearGround() && airTicks > 10) {
            mc.player.networkHandler.sendPacket(
                new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly(true, false)
            );
            airTicks = 0;
        }

        if (currentTick >= delay.get() && isActive()) {
            currentTick = 0;

            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();

            float cosYaw = MathHelper.cos(yaw * ((float) Math.PI / 180.0F));
            float sinYaw = -MathHelper.sin(yaw * ((float) Math.PI / 180.0F));
            float cosPitch = MathHelper.cos(pitch * ((float) Math.PI / 180.0F));
            float sinPitch = -MathHelper.sin(pitch * ((float) Math.PI / 180.0F));

            float hX = sinYaw * cosPitch;
            float hY = sinPitch;
            float hZ = cosYaw * cosPitch;

            double progress = Math.min(1.0, (airTicks + 1) / 3.0);
            double ramp = 1.0 - Math.pow(1.0 - progress, 2.0);

            double horizontalMultiplier = speed.get() * ramp;
            double verticalMultiplier = verticalSpeed.get() * ramp;

            mc.player.addVelocity(
                hX * horizontalMultiplier,
                hY * verticalMultiplier,
                hZ * horizontalMultiplier
            );

            if (nearGround) {
                mc.player.move(MovementType.SELF, new Vec3d(0.0, 0.05, 0.0));
            }
        } else {
            // Air friction when not boosting
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(
                velocity.x * 0.85,
                velocity.y * 0.95,
                velocity.z * 0.85
            );
            currentTick++;
        }
    }
}
