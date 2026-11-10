package com.xlays.c00lgui.modules.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class PlayerESP {

    private static boolean enabled = false;
    private static int color = 0xFFFF3333;
    private static final List<Player> PLAYERS = new ArrayList<>();

    private PlayerESP() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; if (!enabled) PLAYERS.clear(); }
    public static void toggle() { setEnabled(!enabled); }

    public static void tick() {
        if (!enabled) { PLAYERS.clear(); return; }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (mc.level == null || localPlayer == null) { PLAYERS.clear(); return; }

        PLAYERS.clear();
        for (Player player : mc.level.players()) {
            if (player == localPlayer) continue;
            if (!player.isAlive()) continue;
            PLAYERS.add(player);
        }
    }

    public static void render() {
        if (!enabled || PLAYERS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (Player player : PLAYERS) {
            if (player == null || !player.isAlive()) continue;
            GizmoStyle style = GizmoStyle.stroke(color, 2.0F);
            Gizmos.cuboid(player.getBoundingBox(), style);
        }
    }

    public static void disable() { setEnabled(false); }
}