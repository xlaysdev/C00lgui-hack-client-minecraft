package com.xlays.c00lgui;

import net.minecraft.client.Minecraft;

/** Detects the active Minecraft input mode (touchscreen option vs desktop controls). */
public final class DeviceDetector {
    private DeviceDetector() {}
    public static boolean isTouchMode(){
        Minecraft mc=Minecraft.getInstance();
        return mc.options.touchscreen().get();
    }
    public static String getInputLabel(){
        return isTouchMode() ? "TOUCH / MOBILE INPUT" : "KEYBOARD / MOUSE INPUT";
    }
}