package com.xlays.c00lgui;
import com.xlays.c00lgui.config.C00lGuiConfig;
import com.xlays.c00lgui.modules.ModuleManager;
import com.xlays.c00lgui.modules.ModuleSetting;
import com.xlays.c00lgui.modules.visual.OreESP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Modern, touch-friendly ClickGUI with searchable modules and a dedicated settings panel. */
public final class ClickGuiScreen extends Screen {
 private static final int W=660,H=390,HEADER=38,FOOTER=22;
 private static final int BG=0xF00D1017,PANEL=0xFF141923,CARD=0xFF191F2B,HOVER=0xFF252E3D,LINE=0xFF2A3444,TEXT=0xFFF1F5FC,MUTED=0xFF8995A8,GREEN=0xFF55D6A0,ACCENT=0xFF9585FF,RED=0xFFFF7185;
 private static final String[] CATS={"COMBAT","MOVEMENT","PLAYER","VISUAL"},LABELS={"Combat","Movement","Player","Visual"};
 private static final int[] COLORS={0xFFFF7185,0xFF69A9FF,0xFFFFC86B,0xFFB58CFF};
 private float scale=1,ox,oy;private String category="COMBAT",selected=null,search="";private boolean searchFocused=false;
 public ClickGuiScreen(){super(Component.literal("C00LGUI"));}
 @Override protected void init(){super.init();scale=Math.max(.1F,Math.min(1F,Math.min((width-16F)/W,(height-16F)/H)));ox=(width-W*scale)/2;oy=(height-H*scale)/2;}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  g.fill(0,0,width,height,0xB9000000);g.pose().pushMatrix();g.pose().translate(ox,oy);g.pose().scale(scale,scale);
  g.fill(0,0,W,H,BG);border(g,0,0,W,H,LINE);g.fill(0,0,W,HEADER,0xFF171D29);g.fill(0,HEADER-2,W,HEADER,ACCENT);
  g.fill(14,10,32,28,ACCENT);g.drawString(font,"C",20,15,0xFFFFFFFF,false);g.drawString(font,"C00LGUI",40,10,TEXT,false);g.drawString(font,"CLIENT  /  MODULES",104,12,MUTED,false);g.drawString(font,"1.21.11",W-82,13,MUTED,false);
  int close=W-28;g.fill(close,9,close+17,27,hit(mx,my,close,9,17,18)?0xFF633743:0xFF332630);g.drawString(font,"X",close+6,14,RED,false);
  g.fill(0,HEADER,118,H-FOOTER,PANEL);g.drawString(font,"WORKSPACE",14,52,MUTED,false);
  for(int i=0;i<4;i++){int y=67+i*39;boolean active=category.equals(CATS[i]);g.fill(9,y,109,y+31,active?0xFF252E40:hit(mx,my,9,y,100,31)?HOVER:PANEL);if(active)g.fill(9,y,12,y+31,COLORS[i]);g.drawString(font,new String[]{"C","M","P","V"}[i],19,y+10,COLORS[i],false);g.drawString(font,LABELS[i],34,y+10,active?TEXT:MUTED,false);String n=""+ModuleManager.getVisibleModules(CATS[i]).size();g.drawString(font,n,98-font.width(n),y+10,MUTED,false);}
  g.drawString(font,"STATUS",14,H-67,MUTED,false);g.fill(10,H-54,108,H-27,0xFF1C2930);g.fill(17,H-44,22,H-39,GREEN);g.drawString(font,"READY",29,H-45,GREEN,false);
  int x0=130;g.drawString(font,LABELS[idx(category)].toUpperCase()+" MODULES",x0,51,TEXT,false);g.drawString(font,"Tap to toggle  ·  gear to configure",x0,64,MUTED,false);
  g.fill(x0,82,x0+246,105,0xFF171E29);border(g,x0,82,246,23,LINE);g.drawString(font,search.isEmpty()?"Search modules...":search,x0+8,90,searchFocused?TEXT:MUTED,false);
  List<String> mods=new ArrayList<>();for(String n:ModuleManager.getVisibleModules(category))if(n.toLowerCase().contains(search.toLowerCase()))mods.add(n);
  for(int i=0;i<mods.size();i++){String n=mods.get(i);int x=x0+(i%2)*126,y=116+(i/2)*50;boolean on=ModuleManager.isEnabled(n);g.fill(x,y,x+118,y+43,on?0xFF1D302D:hit(mx,my,x,y,118,43)?HOVER:CARD);border(g,x,y,118,43,on?0xFF376D5C:LINE);g.fill(x+7,y+8,x+9,y+35,on?GREEN:0xFF485366);g.drawString(font,n,x+15,y+8,on?TEXT:MUTED,false);g.drawString(font,on?"ENABLED":"DISABLED",x+15,y+25,on?GREEN:MUTED,false);g.fill(x+98,y+5,x+113,y+20,selected!=null&&selected.equals(n)?0xFF403C67:0xFF252D3B);g.drawString(font,"*",x+103,y+8,ACCENT,false);}
  if(mods.isEmpty())g.drawString(font,"No matching modules.",x0+8,124,MUTED,false);
  int px=390,py=51,pw=256,ph=H-HEADER-FOOTER-13;g.fill(px,py,px+pw,py+ph,PANEL);border(g,px,py,pw,ph,LINE);g.fill(px,py,px+pw,py+31,0xFF1B2230);g.drawString(font,"MODULE SETTINGS",px+11,62,MUTED,false);
  if(selected==null||!ModuleManager.getVisibleModules(category).contains(selected)){g.drawString(font,"Choose a module",px+13,108,TEXT,false);g.drawString(font,"Tap a module's gear icon to",px+13,127,MUTED,false);g.drawString(font,"view and change its settings.",px+13,140,MUTED,false);}
  else{boolean on=ModuleManager.isEnabled(selected);g.drawString(font,selected,px+12,94,TEXT,false);g.fill(px+pw-62,py+37,px+pw-12,py+56,on?0xFF214B3D:0xFF303846);g.drawString(font,on?"ON":"OFF",px+pw-47,py+44,on?GREEN:MUTED,false);int yy=py+68;
   for(ModuleSetting s:ModuleManager.getSettings(selected)){g.drawString(font,s.getName(),px+12,yy+5,TEXT,false);if(s.getType()==ModuleSetting.Type.BOOL){boolean v=s.getValue() instanceof Boolean b&&b;int bx=px+pw-62;g.fill(bx,yy,bx+50,yy+19,v?0xFF214B3D:0xFF303846);g.drawString(font,v?"ON":"OFF",bx+15,yy+6,v?GREEN:MUTED,false);}else{int bx=px+pw-60;g.fill(bx,yy,bx+18,yy+19,0xFF303846);g.fill(bx+43,yy,bx+61,yy+19,0xFF303846);g.drawString(font,"-",bx+7,yy+6,TEXT,false);g.drawString(font,"+",bx+49,yy+6,TEXT,false);String v=s.displayValue();g.drawString(font,v,bx+22-font.width(v)/2,yy+6,ACCENT,false);}yy+=25;}
   if("OreESP".equals(selected)){g.drawString(font,"ORE FILTERS",px+12,yy+4,MUTED,false);yy+=15;for(OreESP.OreGroup group:OreESP.OreGroup.values()){boolean v=OreESP.isOreEnabled(group);g.fill(px+12,yy,px+pw-12,yy+18,v?0xFF1E2B2A:0xFF1B202A);g.drawString(font,group.getLabel(),px+18,yy+5,v?TEXT:MUTED,false);g.drawString(font,v?"ON":"OFF",px+pw-43,yy+5,v?GREEN:MUTED,false);yy+=20;}}
  }
  g.fill(118,H-FOOTER,W,H,0xFF171D29);g.drawString(font,"RIGHT SHIFT  /  ESC  ·  CLOSE",132,H-14,MUTED,false);g.drawString(font,ModuleManager.getEnabledModules().size()+" ACTIVE",W-86,H-14,GREEN,false);
  g.pose().popMatrix();super.render(g,mx,my,pt);
 }
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){int x=(int)((e.x()-ox)/scale),y=(int)((e.y()-oy)/scale),b=e.button();if(b==0&&hit(x,y,W-28,9,17,18)){closeGui();return true;}
  for(int i=0;i<4;i++)if(b==0&&hit(x,y,9,67+i*39,100,31)){category=CATS[i];selected=null;search="";searchFocused=false;return true;}
  if(b==0&&hit(x,y,130,82,246,23)){searchFocused=true;return true;}
  if(b==0){List<String> mods=new ArrayList<>();for(String n:ModuleManager.getVisibleModules(category))if(n.toLowerCase().contains(search.toLowerCase()))mods.add(n);for(int i=0;i<mods.size();i++){int bx=130+(i%2)*126,by=116+(i/2)*50;if(hit(x,y,bx,by,118,43)){String n=mods.get(i);if(x>=bx+96)selected=n;else{ModuleManager.toggle(n);selected=n;C00lGuiConfig.save();}return true;}}}
  if(b==0&&selected!=null){int px=390,pw=256,yy=119;for(ModuleSetting s:ModuleManager.getSettings(selected)){if(s.getType()==ModuleSetting.Type.BOOL&&hit(x,y,px+pw-62,yy,50,19)){s.toggleBool();C00lGuiConfig.save();return true;}if(s.getType()!=ModuleSetting.Type.BOOL){int bx=px+pw-60;double step=s.getType()==ModuleSetting.Type.INT?1:Math.max(.01,(s.getMax()-s.getMin())/100);if(hit(x,y,bx,yy,18,19)){s.adjust(-step);C00lGuiConfig.save();return true;}if(hit(x,y,bx+43,yy,18,19)){s.adjust(step);C00lGuiConfig.save();return true;}}yy+=25;}if("OreESP".equals(selected)){yy+=15;for(OreESP.OreGroup group:OreESP.OreGroup.values()){if(hit(x,y,402,yy,232,18)){OreESP.toggleOre(group);C00lGuiConfig.save();return true;}yy+=20;}}}
  return true;
 }
 @Override public boolean mouseReleased(MouseButtonEvent e){return true;}
 @Override public boolean keyPressed(KeyEvent e){int k=e.key();if(k==GLFW.GLFW_KEY_ESCAPE){if(searchFocused){searchFocused=false;return true;}closeGui();return true;}if(k==GLFW.GLFW_KEY_RIGHT_SHIFT){closeGui();return true;}if(searchFocused){if(k==GLFW.GLFW_KEY_BACKSPACE){if(!search.isEmpty())search=search.substring(0,search.length()-1);return true;}if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z){search+=(char)('a'+k-GLFW.GLFW_KEY_A);return true;}if(k>=GLFW.GLFW_KEY_0&&k<=GLFW.GLFW_KEY_9){search+=(char)('0'+k-GLFW.GLFW_KEY_0);return true;}if(k==GLFW.GLFW_KEY_SPACE){search+=" ";return true;}return true;}return super.keyPressed(e);}
 private void closeGui(){C00lGuiConfig.save();Minecraft.getInstance().setScreen(null);}
 @Override public boolean isPauseScreen(){return false;}
 private static int idx(String s){for(int i=0;i<CATS.length;i++)if(CATS[i].equals(s))return i;return 0;}
 private static boolean hit(int a,int b,int x,int y,int w,int h){return a>=x&&a<x+w&&b>=y&&b<y+h;}
 private static void border(GuiGraphics g,int x,int y,int w,int h,int c){g.fill(x,y,x+w,y+1,c);g.fill(x,y+h-1,x+w,y+h,c);g.fill(x,y,x+1,y+h,c);g.fill(x+w-1,y,x+w,y+h,c);}
}