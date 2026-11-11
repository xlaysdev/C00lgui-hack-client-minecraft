package com.xlays.c00lgui.modules.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class OreESP {

    private static boolean enabled = false;
    private static int radius = 12;
    private static int tickCounter = 0;
    private static final List<BlockPos> ORES = new ArrayList<>();

    private static final List<Block> TARGET_BLOCKS = List.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.ANCIENT_DEBRIS
    );

    private OreESP() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            ORES.clear();
            tickCounter = 0;
        }
    }
    public static void toggle() { setEnabled(!enabled); }

    // =====================================================================
    // Settings
    // =====================================================================
    public static int getRadius() {
        return radius;
    }

    public static void setRadius(int value) {
        radius = Math.max(4, Math.min(32, value));
    }

    // =====================================================================
    // Tick
    // =====================================================================
    public static void tick() {
        if (!enabled) {
            ORES.clear();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            ORES.clear();
            return;
        }

        tickCounter++;
        if (tickCounter < 10) return;
        tickCounter = 0;

        BlockPos center = player.blockPosition();
        List<BlockPos> found = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    Block block = mc.level.getBlockState(pos).getBlock();
                    if (TARGET_BLOCKS.contains(block)) {
                        found.add(pos.immutable());
                    }
                }
            }
        }
        ORES.clear();
        ORES.addAll(found);
    }

    // =====================================================================
    // Render
    // =====================================================================
    public static void render() {
        if (!enabled || ORES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (BlockPos pos : ORES) {
            Block block = mc.level.getBlockState(pos).getBlock();
            int color = getColor(block);
            GizmoStyle style = GizmoStyle.stroke(color, 2.0F);
            Gizmos.cuboid(pos, style);
        }
    }

    public static int getColor(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return 0xFF55FFFF;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 0xFF55FF55;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return 0xFFFFD700;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 0xFFFFAA66;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 0xFFFF8844;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 0xFFFF2222;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return 0xFF4444FF;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 0xFF333333;
        if (block == Blocks.ANCIENT_DEBRIS) return 0xFFAA55AA;
        return 0xFFFFFFFF;
    }

    public static void disable() { setEnabled(false); }
}