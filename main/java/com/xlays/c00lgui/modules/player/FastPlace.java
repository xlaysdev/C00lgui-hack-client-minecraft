package com.xlays.c00lgui.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BlockItem;

import java.lang.reflect.Field;

public final class FastPlace {
    private static boolean enabled = false;
    private static int placeDelay = 2;
    private static boolean instantPlace = false;
    private static Field rightClickDelayField;

    private FastPlace() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static void toggle() { setEnabled(!enabled); }

    public static int getPlaceDelay() { return placeDelay; }
    public static void setPlaceDelay(int value) {
        placeDelay = Math.max(1, Math.min(10, value));
    }

    public static boolean isInstantPlace() { return instantPlace; }
    public static void setInstantPlace(boolean value) { instantPlace = value; }

    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options == null || mc.screen != null) return;
        if (!mc.options.keyUse.isDown()
                || !(player.getMainHandItem().getItem() instanceof BlockItem)) return;

        try {
            if (rightClickDelayField == null) {
                rightClickDelayField = findDelayField();
                if (rightClickDelayField == null) return;
                rightClickDelayField.setAccessible(true);
            }

            int currentDelay = rightClickDelayField.getInt(mc);
            int desiredDelay = instantPlace ? 0 : placeDelay;
            if (currentDelay > desiredDelay) {
                rightClickDelayField.setInt(mc, desiredDelay);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            rightClickDelayField = null;
        }
    }

    private static Field findDelayField() {
        for (String name : new String[]{"rightClickDelay", "f_91074_"}) {
            try {
                return Minecraft.class.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Try the next known mapping.
            }
        }
        return null;
    }

    public static void disable() { setEnabled(false); }
}