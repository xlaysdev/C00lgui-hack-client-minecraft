package com.xlays.c00lgui.modules.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class CriticalHUD {

    private static boolean enabled = true;
    private static final List<HitIndicator> indicators = new ArrayList<>();
    private static boolean wasAttacking = false;

    private static class HitIndicator {
        String text;
        int x, y;
        float life;
        int color;
        float velocityY;

        HitIndicator(String text, int x, int y, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.life = 1.0F;
            this.color = color;
            this.velocityY = -1.0F;
        }

        boolean update() {
            life -= 0.03F;
            y += velocityY;
            velocityY -= 0.05F;
            return life > 0;
        }
    }

    private CriticalHUD() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; if (!enabled) indicators.clear(); }
    public static void toggle() { enabled = !enabled; }

    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        boolean isAttacking = mc.options.keyAttack.isDown();
        boolean isFalling = player.getDeltaMovement().y < -0.001;
        boolean isInAir = !player.onGround();

        if (isAttacking && !wasAttacking && isInAir && isFalling && !player.isInWater()) {
            HitResult hitResult = mc.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                addCriticalIndicator();
            }
        }

        wasAttacking = isAttacking;
        indicators.removeIf(indicator -> !indicator.update());
    }

    private static void addCriticalIndicator() {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = screenWidth / 2 - 40;
        int y = screenHeight / 2 - 20;

        float hue = (System.currentTimeMillis() % 3000) / 3000.0F;
        int color = Color.HSBtoRGB(hue, 0.9F, 1.0F);

        indicators.add(new HitIndicator("⚔ CRITICAL!", x, y, color));
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!enabled || indicators.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics graphics = event.getGuiGraphics();

        for (HitIndicator indicator : indicators) {
            float alpha = Math.min(1.0F, indicator.life * 2.0F);
            int alphaInt = (int)(alpha * 255);
            int color = indicator.color & 0x00FFFFFF | (alphaInt << 24);

            graphics.drawString(
                    mc.font,
                    indicator.text,
                    indicator.x,
                    indicator.y,
                    color,
                    false
            );
        }
    }

    public static void disable() {
        setEnabled(false);
    }
}