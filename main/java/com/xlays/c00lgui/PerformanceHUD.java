package com.xlays.c00lgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Lightweight always-on FPS counter; intentionally not exposed as a GUI module. */
@EventBusSubscriber(modid=ClientMain.MOD_ID,value=Dist.CLIENT)
public final class PerformanceHUD {
    private PerformanceHUD(){}
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.options.hideGui)return;
        GuiGraphics g=event.getGuiGraphics();
        String label="FPS: "+mc.getFps();
        int x=6,y=6;
        g.fill(x-3,y-2,x+mc.font.width(label)+5,y+mc.font.lineHeight+2,0x99000000);
        g.drawString(mc.font,label,x,y,0xFF55E6A5,false);
    }
}