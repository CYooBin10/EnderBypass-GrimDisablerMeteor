package com.cyoo.addon.modules;

import com.cyoo.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class GrimSpeed extends Module {
    private int tickCounter = 0;
    private final SettingGroup sg = settings.getDefaultGroup();

    public GrimSpeed() {
        super(Addon.CATEGORY, "grim-speed", "Bypasses GrimAC anti-speed detection.");
    }

    private final Setting<Boolean> groundSync = sg.add(
        new BoolSetting.Builder()
            .name("ground-sync")
            .description("Sync on-ground state when player is grounded.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> syncInterval = sg.add(
        new IntSetting.Builder()
            .name("sync-interval")
            .description("Ticks between sync packets.")
            .sliderRange(1, 5).defaultValue(2)
            .visible(() -> groundSync.get())
            .build()
    );

    @Override
    public void onActivate() {
        tickCounter = 0;
    }

    @Override
    public void onDeactivate() {
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        tickCounter++;

        if (groundSync.get() && mc.player.isOnGround() && tickCounter % syncInterval.get() == 0) {
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(true, false)
            );
        }
    }
}
