package com.xlays.c00lgui;

import com.xlays.c00lgui.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Compact, low-overhead PvP information HUD.
 * It only displays client-visible status; it does not automate attacks or aiming.
 */
@EventBusSubscriber(modid = ClientMain.MOD_ID, value = Dist.CLIENT)
public final class CombatInfoHUD {
    private static final int X = 6;
    private static final int Y = 29;
    private static final int WIDTH = 176;
    private static final int ROW_HEIGHT = 13;

    private CombatInfoHUD() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!ModuleManager.isEnabled("CombatHUD")
                || mc.player == null
                || mc.options.hideGui
                || mc.screen != null) {
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int playerHealth = Math.max(0, Math.round(mc.player.getHealth()));
        int playerMaxHealth = Math.max(1, Math.round(mc.player.getMaxHealth()));
        int armor = mc.player.getArmorValue();
        int cooldown = Math.round(mc.player.getAttackStrengthScale(0.0F) * 100.0F);

        Entity target = mc.hitResult instanceof EntityHitResult hit ? hit.getEntity() : null;
        boolean hasTarget = target instanceof LivingEntity living && living.isAlive();

        int rows = hasTarget ? 3 : 2;
        int height = 7 + rows * ROW_HEIGHT;
        g.fill(X - 2, Y - 2, X + WIDTH, Y + height, 0xA910141D);
        g.fill(X - 2, Y - 2, X + WIDTH, Y - 1, 0xFF9585FF);

        g.drawString(mc.font,
                "YOU  " + playerHealth + "/" + playerMaxHealth + " HP",
                X + 4, Y + 3, 0xFF75E0B0, false);
        g.drawString(mc.font,
                "ARM " + armor + "   CD " + cooldown + "%",
                X + 4, Y + 3 + ROW_HEIGHT, 0xFFE5EAF4, false);

        if (hasTarget && target instanceof LivingEntity living) {
            String name = living.getName().getString();
            if (name.length() > 14) name = name.substring(0, 14) + "…";
            String targetLine = "TGT  " + name + "  "
                    + Math.max(0, Math.round(living.getHealth())) + "HP";
            g.drawString(mc.font, targetLine, X + 4,
                    Y + 3 + ROW_HEIGHT * 2, 0xFFFFA0B0, false);
        }
    }
}
