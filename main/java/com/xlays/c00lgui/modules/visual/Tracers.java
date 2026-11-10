package com.xlays.c00lgui.modules.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class Tracers {

    private static boolean enabled = false;
    private static int color = 0xFF00FF00;
    private static boolean bottomOrigin = true;
    private static double thickness = 1.5;

    private Tracers() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static void toggle() { enabled = !enabled; }
    public static void disable() { enabled = false; }
    public static void tick() {}
    public static void render() {}

    // =====================================================================
    // Settings
    // =====================================================================
    public static int getColor() { return color; }
    public static void setColor(int value) { color = value; }

    public static boolean isBottomOrigin() { return bottomOrigin; }
    public static void setBottomOrigin(boolean value) { bottomOrigin = value; }

    public static double getThickness() { return thickness; }
    public static void setThickness(double value) {
        thickness = Math.max(0.5, Math.min(5.0, value));
    }

    // =====================================================================
    // Screen-space render
    // =====================================================================
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        if (self == null || mc.level == null) return;

        GuiGraphics g = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // Use the player's eye position + view rotation instead of Camera's API
        float partialTick = 1.0f;

        Vec3 cameraPos = self.getEyePosition(partialTick);
        float camYaw = self.getViewYRot(partialTick);
        float camPitch = self.getViewXRot(partialTick);

        Matrix4f projection = new Matrix4f(
                mc.gameRenderer.getProjectionMatrix(mc.options.fov().get()));
        Matrix4f view = new Matrix4f();
        view.rotate((float) Math.toRadians(camPitch), 1, 0, 0);
        view.rotate((float) Math.toRadians(camYaw + 180.0f), 0, 1, 0);
        view.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        Matrix4f mvp = new Matrix4f(projection).mul(view);

        int originX = screenW / 2;
        int originY = bottomOrigin ? screenH : screenH / 2;

        int argb = color | 0xFF000000;
        int t = Math.max(1, (int) Math.round(thickness));

        for (Player target : mc.level.players()) {
            if (target == self) continue;
            if (!target.isAlive()) continue;

            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);

            Vector4f v = new Vector4f(
                    (float) targetPos.x,
                    (float) targetPos.y,
                    (float) targetPos.z,
                    1.0f
            );
            v.mul(mvp);

            if (v.w <= 0.0f) continue;

            float ndcX = v.x / v.w;
            float ndcY = v.y / v.w;

            float sx = (ndcX * 0.5f + 0.5f) * screenW;
            float sy = (1.0f - (ndcY * 0.5f + 0.5f)) * screenH;

            drawLine(g, originX, originY, (int) sx, (int) sy, t, argb);
        }
    }

    // =====================================================================
    // Line drawing
    // =====================================================================
    private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2,
                                 int thickness, int argb) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int half = thickness / 2;

        int cx = x1, cy = y1;
        int guard = 0;
        while (guard++ < 4096) {
            g.fill(cx - half, cy - half, cx - half + thickness, cy - half + thickness, argb);
            if (cx == x2 && cy == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 < dx)  { err += dx; cy += sy; }
        }
    }
}