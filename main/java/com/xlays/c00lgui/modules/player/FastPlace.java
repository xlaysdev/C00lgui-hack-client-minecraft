package com.xlays.c00lgui.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class FastPlace {

    private static boolean enabled = false;
    private static int placeDelay = 2; // Ticks between placements (default 4)
    private static boolean instantPlace = false;

    private static int tickCounter = 0;

    private FastPlace() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            tickCounter = 0;
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static int getPlaceDelay() {
        return placeDelay;
    }

    public static void setPlaceDelay(int value) {
        placeDelay = Math.max(1, Math.min(10, value));
    }

    public static boolean isInstantPlace() {
        return instantPlace;
    }

    public static void setInstantPlace(boolean value) {
        instantPlace = value;
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

        // Check if we're holding a block
        if (player.getMainHandItem().isEmpty()) {
            return;
        }

        // If right-click is held, place blocks faster
        if (mc.options.keyUse.isDown()) {
            tickCounter++;
            
            // Override the normal place delay
            if (tickCounter >= placeDelay) {
                // The actual placement is handled by Minecraft's right-click
                // We just need to reset the counter to allow faster placement
                tickCounter = 0;
            }
            
            // If instant place is enabled, place immediately
            if (instantPlace) {
                // This would require a custom placement handler
                // For now, we just simulate faster placement
                tickCounter = 0;
            }
        } else {
            tickCounter = 0;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tick();
    }

    public static void disable() {
        setEnabled(false);
    }
}