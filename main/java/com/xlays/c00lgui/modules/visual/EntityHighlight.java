package com.xlays.c00lgui.modules.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class EntityHighlight {

    private static boolean enabled = false;
    private static int color = 0xFFFFFF00;
    private static final List<Player> TARGETS = new ArrayList<>();

    private EntityHighlight() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; if (!enabled) TARGETS.clear(); }
    public static void toggle() { setEnabled(!enabled); }

    public static void tick() {
        if (!enabled) { TARGETS.clear(); return; }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (mc.level == null || localPlayer == null) { TARGETS.clear(); return; }

        TARGETS.clear();
        for (Player player : mc.level.players()) {
            if (player == localPlayer) continue;
            if (!player.isAlive()) continue;
            TARGETS.add(player);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterEntities event) {
        if (!enabled || TARGETS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (Player player : TARGETS) {
            AABB box = player.getBoundingBox();
            GizmoStyle style = GizmoStyle.stroke(color, 2.0F);
            Gizmos.cuboid(box, style).setAlwaysOnTop();
        }
    }

    public static void disable() { setEnabled(false); }
    public static void render() {}
}