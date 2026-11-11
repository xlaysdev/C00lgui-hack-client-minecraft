package com.xlays.c00lgui;

import com.xlays.c00lgui.modules.MacroManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

/** Floating, draggable in-game controls for the PvP macros. */
@EventBusSubscriber(modid=ClientMain.MOD_ID,value=Dist.CLIENT)
public final class PvPMacroHUD {
    private static final int WIDTH=154, HEADER=18, ROW=21, HEIGHT=HEADER+ROW*6+5;
    private static int x=8,y=34;
    private static boolean dragging=false;
    private static double dragOffsetX,dragOffsetY;
    private static final MacroManager.Macro[] MACROS={
        MacroManager.Macro.WIND_CHARGE_MACE,
        MacroManager.Macro.SWORD_MACE,
        MacroManager.Macro.SWORD_WTAP,
        MacroManager.Macro.AXE_STUN_MACE,
        MacroManager.Macro.CRIT_CHAIN
    };
    private static final String[] LABELS={
        "Wind Charge > Mace",
        "Sword > Mace",
        "W-Tap Sword",
        "Axe Stun > Mace",
        "Critical Chain"
    };

    private PvPMacroHUD(){}

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.options.hideGui||mc.screen!=null)return;
        GuiGraphics g=event.getGuiGraphics();
        int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight();
        if(dragging&&mc.mouseHandler.isLeftPressed()){
            x=(int)(mc.mouseHandler.getScaledXPos(mc.getWindow())-dragOffsetX);
            y=(int)(mc.mouseHandler.getScaledYPos(mc.getWindow())-dragOffsetY);
            clamp(sw,sh);
        }
        clamp(sw,sh);
        double mx=mc.mouseHandler.getScaledXPos(mc.getWindow());
        double my=mc.mouseHandler.getScaledYPos(mc.getWindow());
        g.fill(x-2,y-2,x+WIDTH+2,y+HEIGHT+2,0x66000000);
        g.fill(x,y,x+WIDTH,y+HEIGHT,0xE910141D);
        g.fill(x,y,x+WIDTH,y+HEADER,0xFF293449);
        g.drawString(mc.font,"PVP MACROS",x+7,y+5,0xFFF1F4FA,false);
        g.drawString(mc.font,"DRAG",x+WIDTH-30,y+5,0xFF9EABC0,false);
        for(int i=0;i<MACROS.length;i++){
            int ry=y+HEADER+3+i*ROW;
            boolean active=MacroManager.getActive()==MACROS[i];
            boolean hover=inside(mx,my,x+4,ry,WIDTH-8,ROW-2);
            g.fill(x+4,ry,x+WIDTH-4,ry+ROW-2,active?0xFF285A43:hover?0xFF293449:0xFF1B222D);
            g.drawString(mc.font,LABELS[i],x+9,ry+6,active?0xFF70E6A6:0xFFE2E8F2,false);
            g.drawString(mc.font,active?"STOP":"START",x+WIDTH-43,ry+6,active?0xFFFF9A9A:0xFF8FC6FF,false);
        }
        g.drawString(mc.font,MacroManager.isRunning()?"RUNNING":"READY",x+7,y+HEIGHT-9,MacroManager.isRunning()?0xFF70E6A6:0xFF9EABC0,false);
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.screen!=null||mc.options.hideGui||event.getButton()!=GLFW.GLFW_MOUSE_BUTTON_1)return;
        double mx=mc.mouseHandler.getScaledXPos(mc.getWindow());
        double my=mc.mouseHandler.getScaledYPos(mc.getWindow());
        if(event.getAction()==GLFW.GLFW_RELEASE){
            if(dragging){dragging=false;event.setCanceled(true);}
            return;
        }
        if(event.getAction()!=GLFW.GLFW_PRESS)return;
        if(inside(mx,my,x,y,WIDTH,HEADER)){
            dragging=true;dragOffsetX=mx-x;dragOffsetY=my-y;event.setCanceled(true);return;
        }
        for(int i=0;i<MACROS.length;i++){
            int ry=y+HEADER+3+i*ROW;
            if(inside(mx,my,x+4,ry,WIDTH-8,ROW-2)){
                MacroManager.toggle(MACROS[i]);
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean inside(double px,double py,int bx,int by,int bw,int bh){
        return px>=bx&&px<bx+bw&&py>=by&&py<by+bh;
    }
    private static void clamp(int sw,int sh){
        x=Math.max(2,Math.min(x,Math.max(2,sw-WIDTH-2)));
        y=Math.max(2,Math.min(y,Math.max(2,sh-HEIGHT-2)));
    }
}