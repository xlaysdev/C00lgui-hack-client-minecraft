package com.xlays.c00lgui.modules.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class FullBright {

    private static boolean enabled = false;

    private FullBright() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;

        if (!enabled) {
            removeNightVision();
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
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
         * Keep Night Vision active while FullBright is enabled.
         *
         * The short duration is refreshed every tick,
         * so there is no visible expiration.
         */
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        40,
                        0,
                        false,
                        false,
                        false
                )
        );
    }

    private static void removeNightVision() {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        /*
         * Only remove the effect when FullBright is disabled.
         */
        player.removeEffect(MobEffects.NIGHT_VISION);
    }

    public static void disable() {
        setEnabled(false);
    }
}