package com.xlays.c00lgui.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class Jesus {

    private static boolean enabled = false;

    private Jesus() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void tick() {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        if (player.isInWater() && player.getDeltaMovement().y < 0.0D) {
            player.setDeltaMovement(
                    player.getDeltaMovement().x,
                    0.08D,
                    player.getDeltaMovement().z
            );
        }
    }
}