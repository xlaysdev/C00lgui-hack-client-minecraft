package com.xlays.c00lgui.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Fly {

    private static boolean enabled = false;

    // Settings
    private static double horizontalSpeed = 0.5;
    private static double verticalSpeed = 0.5;
    private static boolean preventFallDamage = true;

    private Fly() {}

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            disableFlight();
        }
    }

    public static void toggle() { setEnabled(!enabled); }

    // =====================================================================
    // Settings
    // =====================================================================
    public static double getHorizontalSpeed() { return horizontalSpeed; }

    public static void setHorizontalSpeed(double value) {
        horizontalSpeed = Math.max(0.1, Math.min(10.0, value));
    }

    public static double getVerticalSpeed() { return verticalSpeed; }

    public static void setVerticalSpeed(double value) {
        verticalSpeed = Math.max(0.1, Math.min(10.0, value));
    }

    public static boolean isPreventFallDamage() { return preventFallDamage; }

    public static void setPreventFallDamage(boolean value) {
        preventFallDamage = value;
    }

    // =====================================================================
    // Tick
    // =====================================================================
    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Enable creative-style flight
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.getAbilities().setFlyingSpeed((float) (0.05 * horizontalSpeed));
        player.onUpdateAbilities();

        // Vertical movement via jump / sneak keys
        if (mc.options != null && player.getAbilities().flying) {
            double vSpeed = 0.15 * verticalSpeed;
            Vec3 delta = player.getDeltaMovement();

            double dy = delta.y;
            if (mc.options.keyJump.isDown()) {
                dy = vSpeed;
            } else if (mc.options.keyShift.isDown()) {
                dy = -vSpeed;
            } else {
                // Hover — small gravity offset so you don't drift
                dy = 0.0;
            }

            player.setDeltaMovement(delta.x, dy, delta.z);
        }

        // Fall damage prevention
        if (preventFallDamage) {
            player.resetFallDistance();
        }
    }

    // =====================================================================
    // Disable
    // =====================================================================
    private static void disableFlight() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.getAbilities().setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
            player.resetFallDistance();
        }
    }

    public static void disable() {
        enabled = false;
        disableFlight();
    }
}