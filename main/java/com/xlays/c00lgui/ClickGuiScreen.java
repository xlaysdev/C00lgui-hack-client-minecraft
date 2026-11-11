package com.xlays.c00lgui;

import com.xlays.c00lgui.config.C00lGuiConfig;
import com.xlays.c00lgui.modules.ModuleManager;
import com.xlays.c00lgui.modules.ModuleSetting;
import com.xlays.c00lgui.modules.MacroManager;
import com.xlays.c00lgui.modules.visual.OreESP;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.List;

/** Replacement ClickGUI: category sidebar, module cards and inline settings. */
public final class ClickGuiScreen extends Screen {
    private static final int W=620,H=340,HEADER=30,FOOTER=20;
    private static final int BG=0xE9101218,PANEL=0xFF171A22,ALT=0xFF1D222D;
    private static final int LINE=0xFF303746,TEXT=0xFFF1F4FA,MUTED=0xFF929BAA;
    private static final int GREEN=0xFF55D68B,BLUE=0xFF69A9FF,RED=0xFFFF657A;
    private static final String[] CATS={"COMBAT","MOVEMENT","PLAYER","VISUAL","MACROS"};
    private static final int[] COLORS={0xFFFF7185,0xFF69A9FF,0xFFFFC86B,0xFFB58CFF,0xFF55D6C2};
    private int left,top;
    private float guiScale=1.0F,offsetX,offsetY;
    private String category="COMBAT",selected=null;
    private boolean oreFilterOpen=false;

    public ClickGuiScreen(){super(Component.literal("c00lgui"));}
    @Override protected void init(){
        super.init();
        // Keep the full interface visible on small screens and scale it up slightly on larger ones.
        guiScale=Math.min(1.15F,Math.min((width-24.0F)/W,(height-24.0F)/H));
        guiScale=Math.max(0.1F,guiScale);
        offsetX=(width-W*guiScale)/2.0F;
        offsetY=(height-H*guiScale)/2.0F;
        left=0;
        top=0;
    }

    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.fill(0,0,width,height,0x99000000);
        g.pose().pushMatrix();
        g.pose().translate(offsetX,offsetY);
        g.pose().scale(guiScale,guiScale);
        g.fill(left-2,top-2,left+W+2,top+H+2,0x55000000);
        g.fill(left,top,left+W,top+H,BG); border(g,left,top,W,H,LINE);
        g.fill(left,top,left+W,top+HEADER,ALT);g.fill(left,top+HEADER-1,left+W,top+HEADER,BLUE);
        g.drawString(font,"C00LGUI",left+12,top+10,TEXT,false);
        g.drawString(font,"CLIENT  /  MODULES",left+82,top+11,MUTED,false);
        int cx=left+W-25,cy=top+8;
        g.fill(cx,cy,cx+16,cy+14,hit(mx,my,cx,cy,16,14)?RED:0xFF3A2630);
        g.drawString(font,"X",cx+5,cy+3,TEXT,false);

        int sx=left+10,sy=top+HEADER+12;
        g.drawString(font,"CATEGORIES",sx+4,sy,MUTED,false);
        for(int i=0;i<CATS.length;i++){
            int y=sy+18+i*34;boolean active=category.equals(CATS[i]);
            g.fill(sx,y,sx+102,y+28,active?0xFF293449:hit(mx,my,sx,y,102,28)?0xFF252B37:PANEL);
            if(active)g.fill(sx,y,sx+3,y+28,COLORS[i]);
            g.drawString(font,title(CATS[i]),sx+10,y+10,active?TEXT:MUTED,false);
        }

        if("MACROS".equals(category)){
            drawMacroTab(g,mx,my);
        }else{
            int lx=left+124,ly=top+HEADER+12;
            g.drawString(font,title(category)+" MODULES",lx+2,ly,TEXT,false);
            g.drawString(font,"LMB toggle  /  RMB settings",lx+2,ly+14,MUTED,false);
            List<String> modules=ModuleManager.getVisibleModules(category);
            for(int i=0;i<modules.size();i++){
                int x=lx+(i%2)*119,y=ly+34+(i/2)*41;String name=modules.get(i);
                boolean on=ModuleManager.isEnabled(name);
                g.fill(x,y,x+112,y+34,on?0xFF20372F:hit(mx,my,x,y,112,34)?0xFF252B37:PANEL);
                border(g,x,y,112,34,on?0xFF397B5B:LINE);
                g.fill(x+5,y+7,x+7,y+27,on?GREEN:0xFF586170);
                g.drawString(font,name,x+12,y+8,on?TEXT:MUTED,false);
                g.drawString(font,on?"ON":"OFF",x+84,y+8,on?GREEN:MUTED,false);
            }
    
            int px=left+372,py=top+HEADER+12,pw=236,ph=H-HEADER-FOOTER-24;
            g.fill(px,py,px+pw,py+ph,PANEL);border(g,px,py,pw,ph,LINE);
            g.drawString(font,"MODULE SETTINGS",px+10,py+9,MUTED,false);
            g.fill(px+10,py+25,px+pw-10,py+26,LINE);
            if(selected==null){g.drawString(font,"Right-click a module",px+10,py+46,MUTED,false);g.drawString(font,"to edit its settings.",px+10,py+59,MUTED,false);}
            else{
                g.drawString(font,selected,px+10,py+36,TEXT,false);
                if("OreESP".equals(selected)){
                    int bx=px+10,by=py+48;g.fill(bx,by,bx+pw-20,by+19,oreFilterOpen?0xFF293449:0xFF252B37);border(g,bx,by,pw-20,19,LINE);
                    g.drawString(font,oreFilterOpen?"ORE FILTERS  [OPEN]":"ORE FILTERS  [TAP TO EDIT]",bx+7,by+6,BLUE,false);
                    if(oreFilterOpen){int oy=by+24;for(OreESP.OreGroup group:OreESP.OreGroup.values()){boolean on=OreESP.isOreEnabled(group);g.fill(bx,oy,bx+pw-20,oy+21,on?0xFF202B29:0xFF20232A);g.renderItem(new ItemStack(group.getIcon()),bx+4,oy+2);g.drawString(font,group.getLabel(),bx+26,oy+7,on?TEXT:MUTED,false);g.drawString(font,on?"ON":"OFF",bx+pw-56,oy+7,on?GREEN:MUTED,false);oy+=23;}}
                    else drawCompactSettings(g,px,py,pw,selected);
                }else drawCompactSettings(g,px,py,pw,selected);
            }
    
        }
        int fy=top+H-FOOTER;g.fill(left,fy,left+W,top+H,ALT);
        g.drawString(font,"RIGHT SHIFT / ESC  •  CLOSE",left+12,fy+7,MUTED,false);
        g.drawString(font,ModuleManager.getEnabledModules().size()+" ENABLED",left+W-86,fy+7,GREEN,false);
        g.pose().popMatrix();
        super.render(g,mx,my,partial);
    }

    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){
        int x=(int)((e.x()-offsetX)/guiScale),y=(int)((e.y()-offsetY)/guiScale),b=e.button(),cx=left+W-25;
        if(b==0&&hit(x,y,cx,top+8,16,14)){closeGui();return true;}
        int sx=left+10,sy=top+HEADER+30;
        if(b==0)for(int i=0;i<CATS.length;i++)if(hit(x,y,sx,sy+i*34,102,28)){
            category=CATS[i];selected=null;return true;
        }
        if("MACROS".equals(category)&&b==0){
            int x0=left+124,y0=top+HEADER+12,rowY=y0+44;
            MacroManager.Macro[] macros={MacroManager.Macro.WIND_CHARGE_MACE,MacroManager.Macro.SWORD_MACE,MacroManager.Macro.SWORD_WTAP,MacroManager.Macro.AXE_STUN_MACE,MacroManager.Macro.CRIT_CHAIN,MacroManager.Macro.ELYTRA_MACE};
            for(int i=0;i<macros.length;i++)if(hit(x,y,x0+340,rowY+i*35+3,124,25)){MacroManager.toggle(macros[i]);return true;}
            int slotY=rowY+6*35+3;
            if(hit(x,y,x0+73,slotY,16,18)){MacroManager.cycleChargeSlot(-1);return true;}
            if(hit(x,y,x0+94,slotY,16,18)){MacroManager.cycleChargeSlot(1);return true;}
            if(hit(x,y,x0+190,slotY,16,18)){MacroManager.cycleMaceSlot(-1);return true;}
            if(hit(x,y,x0+211,slotY,16,18)){MacroManager.cycleMaceSlot(1);return true;}
            if(hit(x,y,x0+307,slotY,16,18)){MacroManager.cycleSwordSlot(-1);return true;}
            if(hit(x,y,x0+328,slotY,16,18)){MacroManager.cycleSwordSlot(1);return true;}
            if(hit(x,y,x0+424,slotY,16,18)){MacroManager.cycleAxeSlot(-1);return true;}
            if(hit(x,y,x0+445,slotY,16,18)){MacroManager.cycleAxeSlot(1);return true;}
            return true;
        }
        int lx=left+124,ly=top+HEADER+46;
        List<String> modules=ModuleManager.getVisibleModules(category);
        for(int i=0;i<modules.size();i++){
            int bx=lx+(i%2)*119,by=ly+(i/2)*41;
            if(hit(x,y,bx,by,112,34)){
                String name=modules.get(i);
                if(b==1)selected=name;
                else if(b==0){ModuleManager.toggle(name);C00lGuiConfig.save();}
                return true;
            }
        }
        if(b==0&&selected!=null){
            int px=left+372,py=top+HEADER+12,pw=236;
            if("OreESP".equals(selected)){int bx=px+10,by=py+48;if(hit(x,y,bx,by,pw-20,19)){oreFilterOpen=!oreFilterOpen;return true;}if(oreFilterOpen){int oy=by+24;for(OreESP.OreGroup group:OreESP.OreGroup.values()){if(hit(x,y,bx,oy,pw-20,21)){OreESP.toggleOre(group);C00lGuiConfig.save();return true;}oy+=23;}return true;}}
            int yy=py+55;for(ModuleSetting s:ModuleManager.getSettings(selected)){int cy=yy;if(s.getType()==ModuleSetting.Type.BOOL&&hit(x,y,px+pw-58,cy,48,18)){s.toggleBool();C00lGuiConfig.save();return true;}if(s.getType()!=ModuleSetting.Type.BOOL){double step=s.getType()==ModuleSetting.Type.INT?1.0:Math.max(.01,(s.getMax()-s.getMin())/100.0);if(hit(x,y,px+pw-54,cy,20,18)){s.adjust(-step);C00lGuiConfig.save();return true;}if(hit(x,y,px+pw-25,cy,20,18)){s.adjust(step);C00lGuiConfig.save();return true;}}yy+=25;}
        }
        // Consume every click while this full-screen GUI is open, including blank panel space.
        return true;
    }

    @Override public boolean mouseReleased(MouseButtonEvent e){
        return true;
    }

    @Override public boolean keyPressed(KeyEvent e){
        if(e.key()==GLFW.GLFW_KEY_ESCAPE||e.key()==GLFW.GLFW_KEY_RIGHT_SHIFT){closeGui();return true;}
        return super.keyPressed(e);
    }
    private void drawMacroTab(GuiGraphics g,int mx,int my){
        int x=left+124,y=top+HEADER+12,w=484;
        g.drawString(font,"PVP MACROS",x+2,y,TEXT,false);
        g.drawString(font,"INPUT MODE  /  "+DeviceDetector.getInputLabel(),x+2,y+15,BLUE,false);
        g.fill(x,y+34,x+w,y+35,LINE);
        String[] names={"WIND CHARGE  ->  MACE SLAM","SWORD  ->  MACE COMBO","W-TAP SWORD COMBO","AXE SHIELD STUN  ->  MACE","SWORD CRITICAL CHAIN","ELYTRA  ->  MACE DIVE"};
        String[] desc={"Auto aim, launch, swap and strike on descent","Auto target, sword hit then mace follow-up","Auto aim and sprint reset","Axe hit then mace follow-up","Jump and attack during the fall","Firework boost, auto-dive, unequip Elytra, falling Mace smash"};
        MacroManager.Macro[] macros={MacroManager.Macro.WIND_CHARGE_MACE,MacroManager.Macro.SWORD_MACE,MacroManager.Macro.SWORD_WTAP,MacroManager.Macro.AXE_STUN_MACE,MacroManager.Macro.CRIT_CHAIN,MacroManager.Macro.ELYTRA_MACE};
        int rowY=y+44;
        for(int i=0;i<macros.length;i++){
            int ry=rowY+i*35;boolean active=MacroManager.getActive()==macros[i];
            g.fill(x,ry,x+w,ry+31,PANEL);border(g,x,ry,w,31,LINE);
            g.drawString(font,names[i],x+9,ry+5,TEXT,false);
            g.drawString(font,desc[i],x+9,ry+18,MUTED,false);
            g.fill(x+340,ry+5,x+464,ry+27,active?0xFF69313B:0xFF24563F);
            g.drawString(font,active?"STOP":"START",x+387,ry+11,TEXT,false);
        }
        int sy=rowY+6*35+3;
        g.drawString(font,"CHG "+MacroManager.getChargeSlot(),x+2,sy+5,TEXT,false);
        g.drawString(font,"MACE "+MacroManager.getMaceSlot(),x+119,sy+5,TEXT,false);
        g.drawString(font,"SWORD "+MacroManager.getSwordSlot(),x+236,sy+5,TEXT,false);
        g.drawString(font,"AXE "+MacroManager.getAxeSlot(),x+353,sy+5,TEXT,false);
        int[] minus={x+73,x+190,x+307,x+424};
        for(int bx:minus){g.fill(bx,sy,bx+16,sy+18,ALT);g.drawString(font,"-",bx+5,sy+5,TEXT,false);}
        int[] plus={x+94,x+211,x+328,x+445};
        for(int bx:plus){g.fill(bx,sy,bx+16,sy+18,ALT);g.drawString(font,"+",bx+4,sy+5,TEXT,false);}
        g.drawString(font,MacroManager.getStatus(),x+2,sy+23,MacroManager.isRunning()?GREEN:MUTED,false);
    }

    private void drawCompactSettings(GuiGraphics g,int px,int py,int pw,String module){
        List<ModuleSetting> settings=ModuleManager.getSettings(module);if(settings.isEmpty()){g.drawString(font,"No configurable settings",px+10,py+58,MUTED,false);return;}int y=py+55;
        for(ModuleSetting s:settings){g.drawString(font,s.getName(),px+10,y+5,TEXT,false);if(s.getType()==ModuleSetting.Type.BOOL){boolean v=s.getValue() instanceof Boolean b&&b;int bx=px+pw-58;g.fill(bx,y,bx+48,y+18,v?0xFF24563F:0xFF343B48);g.drawString(font,v?"ON":"OFF",bx+14,y+5,v?GREEN:MUTED,false);}else{int bx=px+pw-54;g.fill(bx,y,bx+20,y+18,0xFF343B48);g.fill(px+pw-25,y,px+pw-5,y+18,0xFF343B48);g.drawString(font,"-",bx+7,y+5,TEXT,false);g.drawString(font,"+",px+pw-18,y+5,TEXT,false);String v=s.displayValue();g.drawString(font,v,px+pw-84-font.width(v)/2,y+5,BLUE,false);}y+=25;}
    }

    private void closeGui(){C00lGuiConfig.save();Minecraft.getInstance().setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
    private static boolean hit(int mx,int my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private static void border(GuiGraphics g,int x,int y,int w,int h,int c){
        g.fill(x,y,x+w,y+1,c);g.fill(x,y+h-1,x+w,y+h,c);g.fill(x,y,x+1,y+h,c);g.fill(x+w-1,y,x+w,y+h,c);
    }
    private static String title(String s){return s.charAt(0)+s.substring(1).toLowerCase();}
}