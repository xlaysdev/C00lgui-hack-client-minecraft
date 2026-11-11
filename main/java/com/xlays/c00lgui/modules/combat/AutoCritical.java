package com.xlays.c00lgui.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class AutoCritical {

    private static boolean enabled = false;
    private static boolean onlyWhenHoldingSword = true;
    private static boolean silentMode = true; // No visual jump

    private static boolean wasAttacking = false;

    private AutoCritical() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static void toggle() { setEnabled(!enabled); }

    public static boolean isOnlyWhenHoldingSword() { return onlyWhenHoldingSword; }
    public static void setOnlyWhenHoldingSword(boolean value) { onlyWhenHoldingSword = value; }

    public static boolean isSilentMode() { return silentMode; }
    public static void setSilentMode(boolean value) { silentMode = value; }

    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (mc.screen != null) return;

        // Check if holding a sword
        if (onlyWhenHoldingSword) {
            String itemName = player.getMainHandItem().getItem().getClass().getSimpleName();
            if (!itemName.contains("Sword") && !itemName.contains("Axe")) {
                return;
            }
        }

        boolean isAttacking = mc.options.keyAttack.isDown();

        // When attacking, force critical hit status
        if (isAttacking && !wasAttacking) {
            // Make player appear as if falling for critical hit
            // This tricks the game into thinking we're in the air
            if (silentMode) {
                // Method 1: Tiny downward motion (invisible to player)
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, -0.01, motion.z);
                
                // Reset fall distance to trigger critical
                player.resetFallDistance();
                
                // Set onGround to false briefly
                player.setOnGround(false);
            }
        }

        wasAttacking = isAttacking;
    }

    public static void disable() {
        setEnabled(false);
    }
}