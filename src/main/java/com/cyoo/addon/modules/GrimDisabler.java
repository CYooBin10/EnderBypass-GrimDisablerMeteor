package com.cyoo.addon.modules;

import com.cyoo.addon.Addon;
import com.cyoo.addon.utils.PacketHelper;
import com.cyoo.addon.utils.TridentChecker;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

public class GrimDisabler extends Module {
    private int currentTick;
    private int postUseTicks;

    private final Random random = new Random();
    private final SettingGroup sg = settings.getDefaultGroup();

    public GrimDisabler() {
        super(Addon.CATEGORY, "grim-disabler", "Optimizes trident packet timing for network stability.");
    }

    private final Setting<Integer> interval = sg.add(
        new IntSetting.Builder()
            .name("interval")
            .description("Ticks between trident use cycles.")
            .sliderRange(0, 15).defaultValue(0)
            .build()
    );

    private final Setting<Boolean> pauseOnEat = sg.add(
        new BoolSetting.Builder()
            .name("pause-on-eat")
            .description("Pause trident use while consuming food.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> slotSpoof = sg.add(
        new BoolSetting.Builder()
            .name("slot-spoof")
            .description("Change selected slot client-side without sending a slot packet.")
            .defaultValue(true)
            .build()
    );

    @Override
    public void onActivate() {
        currentTick = interval.get();
        postUseTicks = 0;
    }

    @Override
    public void onDeactivate() {
        currentTick = 0;
        postUseTicks = 0;
    }

    private boolean isNearGround() {
        if (mc == null || mc.world == null || mc.player == null) return false;
        net.minecraft.util.math.BlockPos below = mc.player.getBlockPos().down();
        return mc.world.getBlockState(below).isSolidBlock(mc.world, below);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc == null || mc.player == null || mc.world == null) return;

        // Post-use vertical sync countdown
        if (postUseTicks > 0) {
            postUseTicks--;

            if (postUseTicks == 0 && (mc.player.isOnGround() || isNearGround())) {
                PacketHelper.sendOnGround(mc, true);
            }

            return;
        }

        // Wait for interval
        if (currentTick < interval.get()) {
            currentTick++;
            return;
        }

        // Randomized interval reset
        currentTick = -(random.nextInt(3) + 1);

        int tridentSlot = TridentChecker.findSlot(mc.player);
        if (tridentSlot == -1) return;
        if (pauseOnEat.get() && mc.player.isUsingItem()) return;
        if (TickRate.INSTANCE.getTimeSinceLastTick() > 0.05f) return;
        if (!TridentChecker.hasRiptideIII(
            mc.world,
            TridentChecker.getStack(mc.player, tridentSlot)
        )) return;

        int oldSlot = mc.player.getInventory().getSelectedSlot();

        if (slotSpoof.get()) {
            mc.player.getInventory().setSelectedSlot(tridentSlot);
        } else {
            InvUtils.swap(tridentSlot, true);
        }

        PacketHelper.sendUseTrident(mc);
        PacketHelper.sendReleaseTrident(mc);

        if (slotSpoof.get()) {
            mc.player.getInventory().setSelectedSlot(oldSlot);
        } else {
            InvUtils.swapBack();
        }

        postUseTicks = 8;
    }
}
