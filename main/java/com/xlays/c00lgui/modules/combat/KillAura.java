package com.xlays.c00lgui.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class KillAura {

    private static boolean enabled = false;
    private static double range = 5.0;
    private static int attackDelay = 5;
    private static boolean players = true;
    private static boolean animals = false;
    private static boolean monsters = true;
    private static boolean throughWalls = false;
    private static boolean autoSwing = true;

    private static int tickCounter = 0;
    private static Entity currentTarget = null;

    private KillAura() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            currentTarget = null;
            tickCounter = 0;
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

    public static int getAttackDelay() {
        return attackDelay;
    }

    public static void setAttackDelay(int value) {
        attackDelay = Math.max(1, Math.min(20, value));
    }

    public static boolean isPlayers() {
        return players;
    }

    public static void setPlayers(boolean value) {
        players = value;
    }

    public static boolean isAnimals() {
        return animals;
    }

    public static void setAnimals(boolean value) {
        animals = value;
    }

    public static boolean isMonsters() {
        return monsters;
    }

    public static void setMonsters(boolean value) {
        monsters = value;
    }

    public static boolean isThroughWalls() {
        return throughWalls;
    }

    public static void setThroughWalls(boolean value) {
        throughWalls = value;
    }

    public static boolean isAutoSwing() {
        return autoSwing;
    }

    public static void setAutoSwing(boolean value) {
        autoSwing = value;
    }

    public static void tick() {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (mc.screen != null) return;

        tickCounter++;
        currentTarget = findTarget(player);

        if (currentTarget == null) return;

        if (tickCounter >= attackDelay) {
            mc.gameMode.attack(player, currentTarget);
            if (autoSwing) {
                player.swing(InteractionHand.MAIN_HAND);
            }
            tickCounter = 0;
        }
    }

    private static Entity findTarget(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        
        AABB searchBox = player.getBoundingBox().inflate(range);
        List<Entity> entities = mc.level.getEntities(
                player,
                searchBox,
                e -> isTargetValid(e, player)
        );

        if (entities.isEmpty()) return null;

        Entity closest = null;
        double closestDistance = range;

        for (Entity entity : entities) {
            if (!throughWalls && !player.hasLineOfSight(entity)) {
                continue;
            }

            double distance = player.distanceTo(entity);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entity;
            }
        }

        return closest;
    }

    private static boolean isTargetValid(Entity entity, LocalPlayer player) {
        if (entity == player) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (!entity.isAlive()) return false;

        if (entity instanceof Player) {
            return players;
        }

        String entityName = entity.getType().getDescription().getString().toLowerCase();
        
        if (entity instanceof Monster) {
            return monsters;
        }

        // Animals
        if (entityName.contains("cow") || entityName.contains("pig") || 
            entityName.contains("chicken") || entityName.contains("sheep") ||
            entityName.contains("rabbit") || entityName.contains("horse") ||
            entityName.contains("wolf") || entityName.contains("cat")) {
            return animals;
        }

        return false;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tick();
    }

    public static void disable() {
        setEnabled(false);
    }
}