package com.xlays.c00lgui.modules.player;

import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "c00lgui", value = Dist.CLIENT)
public final class FastBreak {
    private static boolean enabled = false;
    private static float breakSpeedMultiplier = 2.0F;
    private static boolean instantMining = false;

    private FastBreak() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static void toggle() { setEnabled(!enabled); }

    public static float getBreakSpeedMultiplier() { return breakSpeedMultiplier; }
    public static void setBreakSpeedMultiplier(float value) {
        breakSpeedMultiplier = Math.max(1.0F, Math.min(10.0F, value));
    }

    public static boolean isInstantMining() { return instantMining; }
    public static void setInstantMining(boolean value) { instantMining = value; }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!enabled || !(event.getEntity() instanceof LocalPlayer)) return;

        float original = event.getOriginalSpeed();
        float adjusted = instantMining
                ? Math.max(1000.0F, original)
                : original * breakSpeedMultiplier;
        event.setNewSpeed(adjusted);
    }

    public static void tick() {}
    public static void disable() { setEnabled(false); }
}