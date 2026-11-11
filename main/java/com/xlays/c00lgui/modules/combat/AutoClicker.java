package com.xlays.c00lgui.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Random;

public final class AutoClicker {

    private static boolean enabled = false;
    private static int cps = 12;
    private static boolean leftClick = true;
    private static boolean rightClick = false;
    private static boolean onlyHoldingSword = false;
    private static boolean onlyHoldingTool = false;

    private static long lastClickTime = 0;
    private static final Random random = new Random();

    private AutoClicker() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            lastClickTime = 0;
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static int getCps() {
        return cps;
    }

    public static void setCps(int value) {
        cps = Math.max(1, Math.min(20, value));
    }

    public static boolean isLeftClick() {
        return leftClick;
    }

    public static void setLeftClick(boolean value) {
        leftClick = value;
    }

    public static boolean isRightClick() {
        return rightClick;
    }

    public static void setRightClick(boolean value) {
        rightClick = value;
    }

    public static boolean isOnlyHoldingSword() {
        return onlyHoldingSword;
    }

    public static void setOnlyHoldingSword(boolean value) {
        onlyHoldingSword = value;
    }

    public static boolean isOnlyHoldingTool() {
        return onlyHoldingTool;
    }

    public static void setOnlyHoldingTool(boolean value) {
        onlyHoldingTool = value;
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

        // Check if we should auto-click
        if (!shouldClick(player)) {
            return;
        }

        // Calculate delay based on CPS
        long currentTime = System.currentTimeMillis();
        int delay = (int) (1000.0 / cps);

        // Add some randomness (jitter)
        int jitter = (int) (delay * 0.15);
        int actualDelay = delay + random.nextInt(jitter * 2) - jitter;

        if (currentTime - lastClickTime >= actualDelay) {
            // Perform clicks
            if (leftClick) {
                mc.startAttack();
            }

            if (rightClick) {
                mc.startUseItem();
            }

            lastClickTime = currentTime;
        }
    }

    private static boolean shouldClick(LocalPlayer player) {
        // Check if holding required item
        if (onlyHoldingSword) {
            if (!player.getMainHandItem().getItem().getClass().getSimpleName().contains("Sword")) {
                return false;
            }
        }

        if (onlyHoldingTool) {
            String itemName = player.getMainHandItem().getItem().getClass().getSimpleName();
            if (!itemName.contains("Pickaxe") && !itemName.contains("Axe") && 
                !itemName.contains("Shovel") && !itemName.contains("Hoe")) {
                return false;
            }
        }

        // Don't click if inventory is open
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return false;
        }

        return true;
    }

    public static void disable() {
        setEnabled(false);
    }
}