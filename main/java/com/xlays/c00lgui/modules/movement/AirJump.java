package com.xlays.c00lgui.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class AirJump {

    private static boolean enabled = false;

    private AirJump() {
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

        if (player == null || player.input == null) {
            return;
        }

        /*
         * Allow the jump input to work while airborne.
         *
         * We only change the grounded state while the
         * jump key is being held.
         */
        if (player.input.keyPresses.jump()
                && !player.onGround()) {

            player.setOnGround(true);
        }
    }
}