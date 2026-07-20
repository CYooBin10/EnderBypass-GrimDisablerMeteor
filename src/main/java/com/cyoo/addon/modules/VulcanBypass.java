package com.cyoo.addon.modules;

import com.cyoo.addon.Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class VulcanBypass extends Module {
    private int tickCounter = 0;
    private boolean receivedPositionReset = false;
    private final SettingGroup sg = settings.getDefaultGroup();

    public VulcanBypass() {
        super(Addon.CATEGORY, "vulcan-bypass", "Stabilizes client-server synchronization for Vulcan servers.");
    }

    private final Setting<Boolean> filterAlerts = sg.add(
        new BoolSetting.Builder()
            .name("filter-alerts")
            .description("Hide server alert messages from chat.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> positionSync = sg.add(
        new BoolSetting.Builder()
            .name("position-sync")
            .description("Send position sync packets to maintain server alignment.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> syncInterval = sg.add(
        new IntSetting.Builder()
            .name("sync-interval")
            .description("Ticks between sync packets.")
            .sliderRange(1, 5).defaultValue(2)
            .visible(() -> positionSync.get())
            .build()
    );

    private final Setting<Boolean> handleTeleport = sg.add(
        new BoolSetting.Builder()
            .name("handle-teleport")
            .description("Respond to server teleport commands immediately.")
            .defaultValue(true)
            .build()
    );

    @Override
    public void onActivate() {
        tickCounter = 0;
        receivedPositionReset = false;
    }

    @Override
    public void onDeactivate() {
        tickCounter = 0;
        receivedPositionReset = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        tickCounter++;

        if (positionSync.get() && tickCounter % syncInterval.get() == 0) {
            mc.player.networkHandler.sendPacket(
                new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.isOnGround(), mc.player.horizontalCollision
                )
            );
        }

        // Respond to server teleport: send exact position back
        if (receivedPositionReset && handleTeleport.get()) {
            mc.player.networkHandler.sendPacket(
                new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.isOnGround(), mc.player.horizontalCollision
                )
            );
            receivedPositionReset = false;
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        // Handle server teleport — Vulcan uses this to reset player position
        if (handleTeleport.get() && event.packet instanceof PlayerPositionLookS2CPacket) {
            receivedPositionReset = true;
        }

        // Filter alert messages
        if (filterAlerts.get() && event.packet instanceof GameMessageS2CPacket packet) {
            String msg = packet.content().getString().toLowerCase();
            if (msg.contains("vulcan") || msg.contains("watch")
                || msg.contains("flagged") || msg.contains("violation")
                || msg.contains("alert") || msg.contains("detected")) {
                event.cancel();
            }
        }
    }
}
