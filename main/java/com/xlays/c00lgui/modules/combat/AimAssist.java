package com.xlays.c00lgui.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class AimAssist {

    private static boolean enabled = false;
    private static double range = 6.0;
    private static float speed = 5.0F;
    private static boolean onlyWhenAttacking = false; // Changed to false by default
    private static boolean throughWalls = false;
    private static boolean silentAim = false;

    private static Entity target = null;

    private AimAssist() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            target = null;
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static double getRange() {
        return range;
    }

    public static void setRange(double value) {
        range = Math.max(2.0, Math.min(10.0, value));
    }

    public static float getSpeed() {
        return speed;
    }

    public static void setSpeed(float value) {
        speed = Math.max(0.5F, Math.min(10.0F, value));
    }

    public static boolean isOnlyWhenAttacking() {
        return onlyWhenAttacking;
    }

    public static void setOnlyWhenAttacking(boolean value) {
        onlyWhenAttacking = value;
    }

    public static boolean isThroughWalls() {
        return throughWalls;
    }

    public static void setThroughWalls(boolean value) {
        throughWalls = value;
    }

    public static boolean isSilentAim() {
        return silentAim;
    }

    public static void setSilentAim(boolean value) {
        silentAim = value;
    }

    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        // Don't aim if inventory is open
        if (mc.screen != null) {
            target = null;
            return;
        }

        // Only aim when attacking if enabled
        if (onlyWhenAttacking && !mc.options.keyAttack.isDown()) {
            target = null;
            return;
        }

        // Find the best target
        target = findBestTarget(player);

        if (target == null) return;

        // Aim at the target
        aimAtEntity(player, target);
    }

    private static Entity findBestTarget(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        
        AABB searchBox = player.getBoundingBox().inflate(range);
        List<Entity> entities = mc.level.getEntities(
                player,
                searchBox,
                e -> e instanceof LivingEntity && e.isAlive() && e != player
        );

        if (entities.isEmpty()) return null;

        Entity best = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (!throughWalls && !player.hasLineOfSight(entity)) {
                continue;
            }

            double distance = player.distanceTo(entity);
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() * 0.5, 0).subtract(player.getEyePosition());
            double lookAngle = lookVec.dot(toEntity.normalize());
            
            double score = distance * (2.0 - lookAngle);
            
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    private static void aimAtEntity(LocalPlayer player, Entity target) {
        Vec3 targetPos = target.getBoundingBox().getCenter();
        targetPos = targetPos.add(0, target.getBbHeight() * 0.15, 0);
        
        Vec3 playerPos = player.getEyePosition();
        Vec3 delta = targetPos.subtract(playerPos);
        
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

        float maxStep = speed * 0.8F;
        float yawStep = Math.min(Math.abs(yawDiff), maxStep) * Math.signum(yawDiff);
        float pitchStep = Math.min(Math.abs(pitchDiff), maxStep * 0.6F) * Math.signum(pitchDiff);

        if (!silentAim) {
            player.setYRot(currentYaw + yawStep);
            player.setXRot(currentPitch + pitchStep);
        }
    }

    public static void disable() {
        setEnabled(false);
    }
}