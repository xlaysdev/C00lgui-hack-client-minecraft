package com.xlays.c00lgui.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class Speed {

    private static boolean enabled = false;

    private static double blocksPerSecond = 5.0;

    private Speed() {
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

    public static double getBlocksPerSecond() {
        return blocksPerSecond;
    }

    public static void setBlocksPerSecond(double value) {

        /*
         * Clamp the value between 5 and 15 B/s.
         */
        blocksPerSecond = Math.max(
                5.0,
                Math.min(15.0, value)
        );
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

        /*
         * Don't move the player if there is no movement input.
         */
        if (player.input == null) {
            return;
        }

        float forward = player.input.getMoveVector().y;
        float strafe = player.input.getMoveVector().x;

        if (forward == 0.0F && strafe == 0.0F) {
            return;
        }

        /*
         * Minecraft movement values are units per tick.
         *
         * 20 ticks = roughly 1 second.
         */
        double speedPerTick =
                blocksPerSecond / 20.0;

        /*
         * Preserve the player's direction.
         */
        float yaw = player.getYRot();

        double radians =
                Math.toRadians(yaw);

        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double motionX =
                (-sin * forward + cos * strafe)
                * speedPerTick;

        double motionZ =
                (cos * forward + sin * strafe)
                * speedPerTick;

        player.setDeltaMovement(
                motionX,
                player.getDeltaMovement().y,
                motionZ
        );

        player.setSprinting(true);
    }
}