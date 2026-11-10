package com.xlays.c00lgui.modules.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class Step {

    private static boolean enabled = false;

    private static double stepHeight = 1.0;

    private Step() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        updatePlayer();
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static double getStepHeight() {
        return stepHeight;
    }

    public static void setStepHeight(double value) {
        stepHeight = Math.max(
                0.6,
                Math.min(2.5, value)
        );

        updatePlayer();
    }

    public static void tick() {
        updatePlayer();
    }

    private static void updatePlayer() {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        AttributeInstance attribute =
                player.getAttribute(Attributes.STEP_HEIGHT);

        if (attribute == null) {
            return;
        }

        if (enabled) {
            attribute.setBaseValue(stepHeight);
        } else {
            attribute.setBaseValue(0.6);
        }
    }

    public static void disable() {
        enabled = false;
        updatePlayer();
    }
}