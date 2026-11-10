package com.xlays.c00lgui.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class NoFall {

    private static boolean enabled = false;

    // Minimum fall distance before NoFall kicks in.
    // 0 = prevent damage from any fall.
    private static double minFallDistance = 3.0;

    private NoFall() {}

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            resetPlayer();
        }
    }

    public static void toggle() { setEnabled(!enabled); }

    // =====================================================================
    // Settings
    // =====================================================================
    public static double getMinFallDistance() { return minFallDistance; }

    public static void setMinFallDistance(double value) {
        minFallDistance = Math.max(0, Math.min(10, value));
    }

    // =====================================================================
    // Tick
    // =====================================================================
    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // Reset fall distance continuously while descending past the threshold.
        // This is client-side and prevents damage in singleplayer.
        // On multiplayer servers, the server has its own fall distance tracker;
        // this will not prevent damage there without packet-level tricks.
        if (player.getDeltaMovement().y < 0.0D
                && player.fallDistance >= minFallDistance) {
            player.resetFallDistance();
        }
    }

    // =====================================================================
    // Disable
    // =====================================================================
    private static void resetPlayer() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            player.resetFallDistance();
        }
    }

    public static void disable() {
        enabled = false;
        resetPlayer();
    }
}