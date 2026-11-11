package com.xlays.c00lgui.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Fly {
    private static boolean enabled = false;
    private static double horizontalSpeed = 0.5;
    private static double verticalSpeed = 0.5;
    private static boolean preventFallDamage = true;

    private static LocalPlayer abilityOwner;
    private static boolean previousMayfly;
    private static boolean previousFlying;
    private static float previousFlyingSpeed;

    private Fly() {}

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (enabled) enableFlight();
        else disableFlight();
    }

    public static void toggle() { setEnabled(!enabled); }

    public static double getHorizontalSpeed() { return horizontalSpeed; }
    public static void setHorizontalSpeed(double value) {
        horizontalSpeed = Math.max(0.1, Math.min(10.0, value));
    }

    public static double getVerticalSpeed() { return verticalSpeed; }
    public static void setVerticalSpeed(double value) {
        verticalSpeed = Math.max(0.1, Math.min(10.0, value));
    }

    public static boolean isPreventFallDamage() { return preventFallDamage; }
    public static void setPreventFallDamage(boolean value) { preventFallDamage = value; }

    public static void tick() {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (abilityOwner != player) {
            restoreAbilities();
            rememberAbilities(player);
        }

        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.getAbilities().setFlyingSpeed((float) (0.05 * horizontalSpeed));
        player.onUpdateAbilities();

        if (mc.options != null) {
            double vertical = 0.0;
            if (mc.options.keyJump.isDown()) vertical = 0.15 * verticalSpeed;
            else if (mc.options.keyShift.isDown()) vertical = -0.15 * verticalSpeed;
            Vec3 movement = player.getDeltaMovement();
            player.setDeltaMovement(movement.x, vertical, movement.z);
        }

        if (preventFallDamage) player.resetFallDistance();
    }

    private static void enableFlight() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) rememberAbilities(player);
    }

    private static void rememberAbilities(LocalPlayer player) {
        abilityOwner = player;
        previousMayfly = player.getAbilities().mayfly;
        previousFlying = player.getAbilities().flying;
        previousFlyingSpeed = player.getAbilities().getFlyingSpeed();
    }

    private static void restoreAbilities() {
        LocalPlayer player = abilityOwner;
        if (player == null) return;
        player.getAbilities().mayfly = previousMayfly;
        player.getAbilities().flying = previousFlying;
        player.getAbilities().setFlyingSpeed(previousFlyingSpeed);
        player.onUpdateAbilities();
        abilityOwner = null;
    }

    private static void disableFlight() {
        restoreAbilities();
    }

    public static void disable() { setEnabled(false); }
}