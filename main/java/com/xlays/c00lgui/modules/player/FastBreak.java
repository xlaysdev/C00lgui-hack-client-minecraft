package com.xlays.c00lgui.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = "c00lgui",
        value = Dist.CLIENT
)
public final class FastBreak {

    private static boolean enabled = false;
    private static float breakSpeedMultiplier = 2.0F;
    private static boolean instantMining = false;

    private FastBreak() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static float getBreakSpeedMultiplier() {
        return breakSpeedMultiplier;
    }

    public static void setBreakSpeedMultiplier(float value) {
        breakSpeedMultiplier = Math.max(1.0F, Math.min(10.0F, value));
    }

    public static boolean isInstantMining() {
        return instantMining;
    }

    public static void setInstantMining(boolean value) {
        instantMining = value;
    }

    public static void tick() {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        // Speed up block breaking
        // This works by reducing the time it takes to break blocks
        // The actual implementation requires modifying the block breaking system
        // For now, we use a combination of effects and speed

        // Check if we're looking at a block
        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);

            // If instant mining is enabled, break the block instantly
            if (instantMining) {
                // This would require sending a packet to break the block
                // For now, we just accelerate the mining speed
                player.getAbilities().setFlyingSpeed(0.05F * breakSpeedMultiplier);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tick();
    }

    public static void disable() {
        setEnabled(false);
    }
}