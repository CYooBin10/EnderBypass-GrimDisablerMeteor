package com.cyoo.addon.utils;

import net.minecraft.client.MinecraftClient;

public class AntiFlyManager {
    private int airTicks = 0;
    private boolean hasSentUse = false;

    public void reset() {
        airTicks = 0;
        hasSentUse = false;
    }

    public void onUseSent() {
        hasSentUse = true;
    }

    public boolean shouldSpoofGround(int maxAirTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        if (mc.player.isOnGround()) {
            airTicks = 0;
            hasSentUse = false;
            return false;
        }

        airTicks++;
        return hasSentUse && airTicks >= maxAirTicks;
    }
}
