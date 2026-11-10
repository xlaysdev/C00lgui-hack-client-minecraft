package com.xlays.c00lgui.modules.visual;

import com.xlays.c00lgui.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class RainbowHUD {

    // Animation state
    private static float hueOffset = 0.0F;

    // Settings
    private static boolean enabled = true;
    private static boolean rainbow = true;
    private static float speed = 0.5F;
    private static float scale = 1.0F;
    private static int offsetX = 0;
    private static int offsetY = 0;
    private static boolean shadowEnabled = true;
    private static boolean sortByLength = true;

    private RainbowHUD() {}

    // ===== Enabled =====
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static void toggle() { enabled = !enabled; }

    // ===== Rainbow =====
    public static boolean isRainbow() { return rainbow; }
    public static void setRainbow(boolean value) { rainbow = value; }

    // ===== Speed =====
    public static float getSpeed() { return speed; }
    public static void setSpeed(float value) { speed = clamp(value, 0.1F, 2.0F); }

    // ===== Scale =====
    public static float getScale() { return scale; }
    public static void setScale(float value) { scale = clamp(value, 0.5F, 2.0F); }

    // ===== Offset =====
    public static int getOffsetX() { return offsetX; }
    public static void setOffsetX(int value) { offsetX = value; }

    public static int getOffsetY() { return offsetY; }
    public static void setOffsetY(int value) { offsetY = value; }

    // ===== Shadow =====
    public static boolean isShadowEnabled() { return shadowEnabled; }
    public static void setShadowEnabled(boolean value) { shadowEnabled = value; }

    // ===== Sort =====
    public static boolean isSortByLength() { return sortByLength; }
    public static void setSortByLength(boolean value) { sortByLength = value; }

    // =====================================================================
    // RENDER
    // =====================================================================
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options == null) return;

        List<String> enabledModules = ModuleManager.getEnabledModules();
        if (enabledModules.isEmpty()) return;

        if (sortByLength) {
            enabledModules.sort((a, b) -> Integer.compare(b.length(), a.length()));
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int marginX = 8;
        int marginY = 8;
        int baseX = screenWidth - marginX + offsetX;
        int baseY = marginY + offsetY;

        // Advance hue once per frame — no per-module allocation
        hueOffset = (hueOffset + 0.005F * speed) % 1.0F;

        int lineHeight = Math.max(1, Math.round((mc.font.lineHeight + 2) * scale));

        for (int i = 0; i < enabledModules.size(); i++) {
            String module = enabledModules.get(i);

            int color;
            if (rainbow) {
                float hue = (hueOffset + i * 0.12F) % 1.0F;
                color = hsbToRgbInt(hue, 0.9F, 1.0F);
            } else {
                color = 0xFFFFFFFF;
            }

            int textWidth = Math.round(mc.font.width(module) * scale);
            int x = baseX - textWidth;
            int y = baseY + i * lineHeight;

            if (shadowEnabled) {
                graphics.drawString(mc.font, module, x + 1, y + 1, 0xB0000000, false);
            }

            graphics.drawString(mc.font, module, x, y, color, false);
        }
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    /**
     * Returns a fully opaque 0xAARRGGBB int for the given HSV.
     * Uses java.awt.Color.HSBtoRGB — same method you already used.
     */
    private static int hsbToRgbInt(float h, float s, float v) {
        return java.awt.Color.HSBtoRGB(h, s, v);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}