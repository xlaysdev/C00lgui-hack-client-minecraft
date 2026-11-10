package com.xlays.c00lgui.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class Scaffold {

    private static boolean enabled = false;
    private static boolean placeUnderFeet = true;
    private static boolean placeAhead = true;
    private static boolean rotateToPlace = true;
    private static boolean towerMode = false;
    private static int placeDelay = 2;

    private static int tickCounter = 0;
    private static BlockPos lastPlacedPos = null;
    private static Field selectedField = null;

    private Scaffold() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            tickCounter = 0;
            lastPlacedPos = null;
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static boolean isPlaceUnderFeet() {
        return placeUnderFeet;
    }

    public static void setPlaceUnderFeet(boolean value) {
        placeUnderFeet = value;
    }

    public static boolean isPlaceAhead() {
        return placeAhead;
    }

    public static void setPlaceAhead(boolean value) {
        placeAhead = value;
    }

    public static boolean isRotateToPlace() {
        return rotateToPlace;
    }

    public static void setRotateToPlace(boolean value) {
        rotateToPlace = value;
    }

    public static boolean isTowerMode() {
        return towerMode;
    }

    public static void setTowerMode(boolean value) {
        towerMode = value;
    }

    public static int getPlaceDelay() {
        return placeDelay;
    }

    public static void setPlaceDelay(int value) {
        placeDelay = Math.max(1, Math.min(10, value));
    }

    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (mc.screen != null) return;

        int blocks = getBlockCount(player);
        if (blocks <= 0) return;

        tickCounter++;
        if (tickCounter < placeDelay) return;
        tickCounter = 0;

        BlockPos placePos = null;

        if (placeUnderFeet) {
            BlockPos below = player.blockPosition().below();
            if (mc.level.isEmptyBlock(below)) {
                placePos = below;
            }
        }

        if (placeAhead && placePos == null) {
            BlockPos ahead = getBlockAhead(player);
            if (ahead != null && mc.level.isEmptyBlock(ahead)) {
                placePos = ahead;
            }
        }

        if (towerMode && placePos == null) {
            BlockPos above = player.blockPosition().above();
            if (mc.level.isEmptyBlock(above)) {
                placePos = above;
            }
        }

        if (placePos == null) return;

        placeBlock(mc, player, placePos);
    }

    private static int getBlockCount(LocalPlayer player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static BlockPos getBlockAhead(LocalPlayer player) {
        Direction facing = player.getDirection();
        BlockPos pos = player.blockPosition();
        
        BlockPos ahead = pos.relative(facing);
        if (!player.level().isEmptyBlock(ahead)) {
            ahead = pos.relative(facing, 2);
            if (!player.level().isEmptyBlock(ahead)) {
                return null;
            }
        }
        return ahead;
    }

    private static void placeBlock(Minecraft mc, LocalPlayer player, BlockPos pos) {
        Direction placeFace = Direction.UP;
        BlockPos targetPos = pos.below();

        Direction[] directions = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : directions) {
            BlockPos checkPos = pos.relative(dir);
            if (!mc.level.isEmptyBlock(checkPos) && mc.level.getBlockState(checkPos).isSolid()) {
                targetPos = checkPos;
                placeFace = dir.getOpposite();
                break;
            }
        }

        if (targetPos == pos.below() && mc.level.isEmptyBlock(pos.below())) {
            BlockPos below = pos.below();
            BlockPos belowBelow = below.below();
            if (!mc.level.isEmptyBlock(belowBelow) && mc.level.getBlockState(belowBelow).isSolid()) {
                targetPos = belowBelow;
                placeFace = Direction.UP;
            } else {
                return;
            }
        }

        if (rotateToPlace) {
            Vec3 targetVec = new Vec3(
                    targetPos.getX() + 0.5,
                    targetPos.getY() + 0.5,
                    targetPos.getZ() + 0.5
            );
            rotateToPosition(player, targetVec);
        }

        BlockHitResult hitResult = new BlockHitResult(
                new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5),
                placeFace,
                targetPos,
                false
        );

        int blockSlot = findBlockSlot(player);
        if (blockSlot == -1) return;

        // Set the selected slot using reflection
        setSelectedSlot(player, blockSlot);

        // Place the block
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        lastPlacedPos = pos;
    }

    private static int findBlockSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    private static void setSelectedSlot(LocalPlayer player, int slot) {
        try {
            // Try to use the setSelectedSlot method if it exists
            Method method = net.minecraft.world.entity.player.Inventory.class.getDeclaredMethod("setSelectedSlot", int.class);
            method.setAccessible(true);
            method.invoke(player.getInventory(), slot);
            return;
        } catch (Exception e1) {
            // Fallback: use reflection on the field
            try {
                if (selectedField == null) {
                    selectedField = net.minecraft.world.entity.player.Inventory.class.getDeclaredField("selected");
                    selectedField.setAccessible(true);
                }
                selectedField.setInt(player.getInventory(), slot);
            } catch (Exception e2) {
                // Last resort: try to use setSelectedItem
                try {
                    Method setter = net.minecraft.world.entity.player.Inventory.class.getDeclaredMethod("setSelectedItem", int.class);
                    setter.setAccessible(true);
                    setter.invoke(player.getInventory(), slot);
                } catch (Exception e3) {
                    // Ignore
                }
            }
        }
    }

    private static void rotateToPosition(LocalPlayer player, Vec3 target) {
        Vec3 playerPos = player.getEyePosition();
        Vec3 delta = target.subtract(playerPos);

        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(-Math.atan2(dx, dz));
        float targetPitch = (float) Math.toDegrees(-Math.atan2(dy, horizontalDistance));

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        float yawDiff = targetYaw - currentYaw;
        float pitchDiff = targetPitch - currentPitch;

        while (yawDiff > 180.0F) yawDiff -= 360.0F;
        while (yawDiff < -180.0F) yawDiff += 360.0F;

        player.setYRot(currentYaw + yawDiff);
        player.setXRot(currentPitch + pitchDiff);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tick();
    }

    public static void disable() {
        setEnabled(false);
    }
}