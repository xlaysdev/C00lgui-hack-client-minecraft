package com.xlays.c00lgui.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import java.lang.reflect.Field;
import java.util.List;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class Reach {

    private static boolean enabled = false;
    private static double reachDistance = 6.0;
    private static Field attackRangeField = null;

    private Reach() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            resetReachViaReflection();
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static double getReachDistance() {
        return reachDistance;
    }

    public static void setReachDistance(double value) {
        reachDistance = Math.max(3.0, Math.min(8.0, value));
    }

    public static void tick() {
        if (enabled) {
            extendReachViaReflection();
        }
    }

    private static void extendReachViaReflection() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode == null) return;
            
            if (attackRangeField == null) {
                try {
                    attackRangeField = net.minecraft.client.multiplayer.MultiPlayerGameMode.class.getDeclaredField("attackRange");
                } catch (NoSuchFieldException e) {
                    try {
                        attackRangeField = net.minecraft.client.multiplayer.MultiPlayerGameMode.class.getDeclaredField("f_105210_");
                    } catch (NoSuchFieldException ex) {
                        attackRangeField = net.minecraft.client.multiplayer.MultiPlayerGameMode.class.getDeclaredField("reachDistance");
                    }
                }
                attackRangeField.setAccessible(true);
            }
            
            attackRangeField.set(mc.gameMode, reachDistance);
            
        } catch (Exception e) {
            // Fallback: ignore
        }
    }

    private static void resetReachViaReflection() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode == null || attackRangeField == null) return;
            attackRangeField.set(mc.gameMode, 3.0);
        } catch (Exception e) {
            // Ignore
        }
    }

    @SubscribeEvent
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (event.getKeyMapping() == mc.options.keyAttack) {
            Entity target = getTargetEntity();
            if (target != null && target instanceof LivingEntity) {
                mc.gameMode.attack(player, target);
                player.swing(InteractionHand.MAIN_HAND);
                event.setCanceled(true);
            }
        }
    }

    private static Entity getTargetEntity() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return null;

        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);

        AABB searchBox = player.getBoundingBox().inflate(reachDistance);
        List<Entity> entities = mc.level.getEntities(
                player,
                searchBox,
                e -> e instanceof LivingEntity && e.isAlive() && e != player
        );

        Entity closest = null;
        double closestDistance = reachDistance;

        for (Entity entity : entities) {
            AABB entityBox = entity.getBoundingBox();
            Vec3 hitPos = entityBox.clip(eyePosition, eyePosition.add(viewVector.scale(reachDistance))).orElse(null);

            if (hitPos != null) {
                double distance = eyePosition.distanceTo(hitPos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = entity;
                }
            }
        }

        return closest;
    }

    public static void disable() {
        setEnabled(false);
    }
}