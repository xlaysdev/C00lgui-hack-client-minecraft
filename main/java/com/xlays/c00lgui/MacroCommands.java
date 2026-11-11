package com.xlays.c00lgui;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.xlays.c00lgui.modules.MacroManager;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/** Client-only command shortcuts for starting and controlling macros. */
@EventBusSubscriber(modid = ClientMain.MOD_ID, value = Dist.CLIENT)
public final class MacroCommands {
    private MacroCommands() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> root =
                Commands.literal("macro")
                        .executes(ctx -> help());

        root.then(Commands.literal("ely").executes(ctx -> start(MacroManager.Macro.ELYTRA_MACE, "Elytra Mace")));
        root.then(Commands.literal("wind").executes(ctx -> start(MacroManager.Macro.WIND_CHARGE_MACE, "Wind Charge > Mace")));
        root.then(Commands.literal("sword").executes(ctx -> start(MacroManager.Macro.SWORD_MACE, "Sword > Mace")));
        root.then(Commands.literal("wtap").executes(ctx -> start(MacroManager.Macro.SWORD_WTAP, "W-Tap Sword")));
        root.then(Commands.literal("axe").executes(ctx -> start(MacroManager.Macro.AXE_STUN_MACE, "Axe Stun > Mace")));
        root.then(Commands.literal("crit").executes(ctx -> start(MacroManager.Macro.CRIT_CHAIN, "Critical Chain")));
        root.then(Commands.literal("stop").executes(ctx -> {
            MacroManager.stop();
            feedback("Macro stopped.");
            return Command.SINGLE_SUCCESS;
        }));
        root.then(Commands.literal("hud").executes(ctx -> {
            PvPMacroHUD.toggleVisible();
            feedback("Macro HUD " + (PvPMacroHUD.isVisible() ? "enabled." : "hidden."));
            return Command.SINGLE_SUCCESS;
        }).then(Commands.literal("on").executes(ctx -> {
            PvPMacroHUD.setVisible(true);
            feedback("Macro HUD enabled.");
            return Command.SINGLE_SUCCESS;
        })).then(Commands.literal("off").executes(ctx -> {
            PvPMacroHUD.setVisible(false);
            feedback("Macro HUD hidden.");
            return Command.SINGLE_SUCCESS;
        })));
        event.getDispatcher().register(root);
    }

    private static int start(MacroManager.Macro macro, String name) {
        MacroManager.start(macro);
        feedback("Starting " + name + " macro.");
        return Command.SINGLE_SUCCESS;
    }

    private static int help() {
        feedback("Commands: /macro ely, wind, sword, wtap, axe, crit, stop, hud [on|off]");
        return Command.SINGLE_SUCCESS;
    }

    private static void feedback(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("[C00LGUI] " + message), false);
    }
}
