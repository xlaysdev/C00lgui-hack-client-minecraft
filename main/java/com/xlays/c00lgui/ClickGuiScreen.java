package com.xlays.c00lgui;

import com.xlays.c00lgui.config.C00lGuiConfig;
import com.xlays.c00lgui.modules.ModuleManager;
import com.xlays.c00lgui.modules.ModuleSetting;
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
    private static final String[] CATS={"COMBAT","MOVEMENT","PLAYER","VISUAL"};
    private static final int[] COLORS={0xFFFF7185,0xFF69A9FF,0xFFFFC86B,0xFFB58CFF};
    private int left,top;
    private String category="COMBAT",selected=null;

    public ClickGuiScreen(){super(Component.literal("c00lgui"));}
    @Override protected void init(){super.init();left=Math.max(6,(width-W)/2);top=Math.max(6,(height-H)/2);}

    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.fill(0,0,width,height,0x99000000);
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
        if(selected==null){
            g.drawString(font,"Right-click a module",px+10,py+46,MUTED,false);
            g.drawString(font,"to edit its settings.",px+10,py+59,MUTED,false);
        }else{
            g.drawString(font,selected,px+10,py+36,TEXT,false);
            List<ModuleSetting> settings=ModuleManager.getSettings(selected);
            if(settings.isEmpty())g.drawString(font,"No configurable settings",px+10,py+57,MUTED,false);
            int y=py+43;
            for(ModuleSetting s:settings){
                g.drawString(font,s.getName(),px+10,y,TEXT,false);
                int cy2=y+13,ww=pw-20;
                if(s.getType()==ModuleSetting.Type.BOOL){
                    boolean v=s.getValue() instanceof Boolean b&&b;
                    g.fill(px+10,cy2,px+58,cy2+16,v?0xFF24563F:0xFF343B48);
                    g.drawString(font,v?"ON":"OFF",px+25,cy2+4,v?GREEN:MUTED,false);
                }else{
                    g.fill(px+10,cy2,px+32,cy2+16,0xFF343B48);
                    g.fill(px+10+ww-22,cy2,px+10+ww,cy2+16,0xFF343B48);
                    g.drawString(font,"-",px+19,cy2+4,TEXT,false);
                    g.drawString(font,"+",px+ww-4,cy2+4,TEXT,false);
                    String v=s.displayValue();g.drawString(font,v,px+10+(ww-font.width(v))/2,cy2+4,BLUE,false);
                }
                y+=29;
            }
        }
        int fy=top+H-FOOTER;g.fill(left,fy,left+W,top+H,ALT);
        g.drawString(font,"RIGHT SHIFT / ESC  •  CLOSE",left+12,fy+7,MUTED,false);
        g.drawString(font,ModuleManager.getEnabledModules().size()+" ENABLED",left+W-86,fy+7,GREEN,false);
        super.render(g,mx,my,partial);
    }

    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){
        int x=(int)e.x(),y=(int)e.y(),b=e.button(),cx=left+W-25;
        if(b==0&&hit(x,y,cx,top+8,16,14)){closeGui();return true;}
        int sx=left+10,sy=top+HEADER+30;
        if(b==0)for(int i=0;i<CATS.length;i++)if(hit(x,y,sx,sy+i*34,102,28)){
            category=CATS[i];selected=null;return true;
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
            int px=left+392,yy=top+HEADER+55,ww=216;
            for(ModuleSetting s:ModuleManager.getSettings(selected)){
                int cy=yy+12;
                if(s.getType()==ModuleSetting.Type.BOOL&&hit(x,y,px,cy,48,16)){
                    s.toggleBool();C00lGuiConfig.save();return true;
                }
                if(s.getType()!=ModuleSetting.Type.BOOL){
                    double step=s.getType()==ModuleSetting.Type.INT?1.0:Math.max(.01,(s.getMax()-s.getMin())/100.0);
                    if(hit(x,y,px,cy,22,16)){s.adjust(-step);C00lGuiConfig.save();return true;}
                    if(hit(x,y,px+ww-22,cy,22,16)){s.adjust(step);C00lGuiConfig.save();return true;}
                }
                yy+=29;
            }
        }
        return super.mouseClicked(e,doubleClick);
    }

    @Override public boolean keyPressed(KeyEvent e){
        if(e.key()==GLFW.GLFW_KEY_ESCAPE||e.key()==GLFW.GLFW_KEY_RIGHT_SHIFT){closeGui();return true;}
        return super.keyPressed(e);
    }
    private void closeGui(){C00lGuiConfig.save();Minecraft.getInstance().setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
    private static boolean hit(int mx,int my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private static void border(GuiGraphics g,int x,int y,int w,int h,int c){
        g.fill(x,y,x+w,y+1,c);g.fill(x,y+h-1,x+w,y+h,c);g.fill(x,y,x+1,y+h,c);g.fill(x+w-1,y,x+w,y+h,c);
    }
    private static String title(String s){return s.charAt(0)+s.substring(1).toLowerCase();}
}