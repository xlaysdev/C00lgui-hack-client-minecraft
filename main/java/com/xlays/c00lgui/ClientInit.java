package com.xlays.c00lgui;

import com.mojang.blaze3d.platform.InputConstants;
import com.xlays.c00lgui.config.C00lGuiConfig;
import com.xlays.c00lgui.modules.ModuleManager;
import com.xlays.c00lgui.modules.visual.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = ClientMain.MOD_ID,
        value = Dist.CLIENT
)
public final class ClientInit {

    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.MISC;

    public static final KeyMapping OPEN_GUI = new KeyMapping(
            "key.c00lgui.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KEY_CATEGORY
    );

    private ClientInit() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI);
    }

    @SubscribeEvent
    public static void onLogin(LoggingIn event) {
        C00lGuiConfig.load();
    }

    @SubscribeEvent
    public static void onShutdown(GameShuttingDownEvent event) {
        C00lGuiConfig.save();
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        ModuleManager.tick();

        while (OPEN_GUI.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new ClickGuiScreen());
            } else if (mc.screen instanceof C00lGuiScreen) {
                mc.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent.AfterEntities event) {
        OreESP.render();
        PlayerESP.render();
        Tracers.render();
        ESP.render();
        EntityHighlight.render();
    }
}