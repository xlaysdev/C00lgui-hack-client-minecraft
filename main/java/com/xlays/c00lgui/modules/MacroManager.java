package com.xlays.c00lgui.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.AABB;

/** User-started PvP automation: item discovery, camera tracking and tick-driven sequences. */
public final class MacroManager {
    public enum Macro { WIND_CHARGE_MACE, SWORD_MACE, SWORD_WTAP, AXE_STUN_MACE, CRIT_CHAIN, ELYTRA_MACE }

    private static Macro active;
    private static int phase,ticks,timeout;
    private static int chargeSlot=7,maceSlot=8,swordSlot=0,axeSlot=1;
    private static int activeChargeSlot=-1,activeMaceSlot=-1,activeSwordSlot=-1,activeAxeSlot=-1,rocketSlot=-1;
    private static int elytraInventorySlot=-1,elytraStashSlot=-1;
    private static boolean elytraEquippedByMacro,elytraWasAlreadyWorn;
    private static double diveStartY;
    private static float savedPitch,savedYaw;
    private static boolean sprintWasActive;
    private static String status="READY";
    private MacroManager(){}

    public static boolean isRunning(){return active!=null;}
    public static Macro getActive(){return active;}
    public static String getStatus(){return status;}
    public static int getChargeSlot(){return chargeSlot+1;}
    public static int getMaceSlot(){return maceSlot+1;}
    public static int getSwordSlot(){return swordSlot+1;}
    public static int getAxeSlot(){return axeSlot+1;}
    public static void cycleChargeSlot(int d){chargeSlot=wrap(chargeSlot+d);}
    public static void cycleMaceSlot(int d){maceSlot=wrap(maceSlot+d);}
    public static void cycleSwordSlot(int d){swordSlot=wrap(swordSlot+d);}
    public static void cycleAxeSlot(int d){axeSlot=wrap(axeSlot+d);}
    private static int wrap(int n){return Math.floorMod(n,9);}

    public static void start(Macro macro){
        stop();
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null){status="JOIN A WORLD FIRST";return;}
        active=macro;phase=0;ticks=0;timeout=0;
        savedYaw=mc.player.getYRot();savedPitch=mc.player.getXRot();
        sprintWasActive=mc.player.isSprinting();status="SCANNING INVENTORY";
    }
    public static void stop(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player!=null){
            if(active!=null){
                if(elytraEquippedByMacro)unequipElytra(mc.player,mc);
                if(elytraWasAlreadyWorn&&elytraStashSlot>=0)restoreStashedElytra(mc.player,mc);
                mc.player.setYRot(savedYaw);mc.player.setXRot(savedPitch);
            }
            mc.player.setSprinting(sprintWasActive);
        }
        mc.options.keyJump.setDown(false);
        mc.options.keyUp.setDown(false);
        active=null;phase=0;ticks=0;timeout=0;
        activeChargeSlot=activeMaceSlot=activeSwordSlot=activeAxeSlot=rocketSlot=-1;
        elytraInventorySlot=elytraStashSlot=-1;elytraEquippedByMacro=false;elytraWasAlreadyWorn=false;
        if(!status.startsWith("MISSING")&&!status.startsWith("NO "))status="READY";
    }
    public static void toggle(Macro macro){if(active==macro)stop();else start(macro);}

    public static void tick(){
        if(active==null)return;
        Minecraft mc=Minecraft.getInstance();LocalPlayer p=mc.player;
        if(p==null||mc.level==null||mc.gameMode==null||
           (mc.screen!=null&&!(mc.screen instanceof com.xlays.c00lgui.ClickGuiScreen))){stop();return;}
        switch(active){
            case WIND_CHARGE_MACE -> tickWindCharge(p,mc);
            case SWORD_MACE -> tickSwordMace(p,mc);
            case SWORD_WTAP -> tickWTap(p,mc);
            case AXE_STUN_MACE -> tickAxeStun(p,mc);
            case CRIT_CHAIN -> tickCritChain(p,mc);
            case ELYTRA_MACE -> tickElytraMace(p,mc);
        }
    }

    private static void tickWindCharge(LocalPlayer p,Minecraft mc){
        switch(phase){
            case 0 -> {
                activeChargeSlot=findAndEquip(p,mc,"wind_charge",chargeSlot);
                activeMaceSlot=findAndEquip(p,mc,"mace",maceSlot);
                if(activeChargeSlot<0){missing("WIND CHARGE");return;}
                if(activeMaceSlot<0){missing("MACE");return;}
                p.getInventory().setSelectedSlot(activeChargeSlot);p.setXRot(88);phase=1;ticks=0;status="CHARGING";
            }
            case 1 -> {if(++ticks>=2){mc.gameMode.useItem(p,InteractionHand.MAIN_HAND);p.setXRot(savedPitch);phase=2;timeout=0;status="WAITING FOR LAUNCH";}}
            case 2 -> {
                LivingEntity t=findTarget(p,mc,10);
                if(t!=null)aimAt(p,t);
                if(p.getDeltaMovement().y>0.12){phase=3;status="SWITCHING TO MACE";}
                else if(++timeout>45){status="NO LAUNCH - RETRYING";phase=0;timeout=0;}
            }
            case 3 -> {
                p.getInventory().setSelectedSlot(activeMaceSlot);
                LivingEntity t=findTarget(p,mc,10);
                if(t!=null)aimAt(p,t);
                if(p.getDeltaMovement().y < -0.08){attackTarget(p,mc,t);phase=4;ticks=0;status="SLAM";}
            }
            case 4 -> {if(++ticks>=12){phase=0;status="RETRYING";}}
        }
    }

    private static void tickSwordMace(LocalPlayer p,Minecraft mc){
        switch(phase){
            case 0 -> {
                activeSwordSlot=findAndEquip(p,mc,"_sword",swordSlot);activeMaceSlot=findAndEquip(p,mc,"mace",maceSlot);
                if(activeSwordSlot<0){missing("SWORD");return;}if(activeMaceSlot<0){missing("MACE");return;}
                p.getInventory().setSelectedSlot(activeSwordSlot);phase=1;ticks=0;status="SWORD HIT";
            }
            case 1 -> {LivingEntity t=findTarget(p,mc,6);if(t!=null)aimAt(p,t);if(++ticks>=2){attackTarget(p,mc,t);p.getInventory().setSelectedSlot(activeMaceSlot);phase=2;ticks=0;}}
            case 2 -> {LivingEntity t=findTarget(p,mc,6);if(t!=null)aimAt(p,t);if(++ticks>=3){attackTarget(p,mc,t);phase=3;ticks=0;status="MACE FOLLOW-UP";}}
            case 3 -> {if(++ticks>=10){phase=0;status="RETRYING";}}
        }
    }
    private static void tickWTap(LocalPlayer p,Minecraft mc){
        if(phase==0){activeSwordSlot=findAndEquip(p,mc,"_sword",swordSlot);if(activeSwordSlot<0){missing("SWORD");return;}p.getInventory().setSelectedSlot(activeSwordSlot);LivingEntity t=findTarget(p,mc,6);if(t!=null)aimAt(p,t);p.setSprinting(true);attackTarget(p,mc,t);p.setSprinting(false);phase=1;ticks=0;status="W-TAP";}
        else if(phase==1){if(++ticks>=1){p.setSprinting(true);phase=2;ticks=0;}}
        else if(++ticks>=7){phase=0;status="RETRYING";}
    }
    private static void tickAxeStun(LocalPlayer p,Minecraft mc){
        if(phase==0){activeAxeSlot=findAndEquip(p,mc,"_axe",axeSlot);activeMaceSlot=findAndEquip(p,mc,"mace",maceSlot);if(activeAxeSlot<0){missing("AXE");return;}if(activeMaceSlot<0){missing("MACE");return;}p.getInventory().setSelectedSlot(activeAxeSlot);LivingEntity t=findTarget(p,mc,6);if(t!=null)aimAt(p,t);attackTarget(p,mc,t);phase=1;ticks=0;status="AXE HIT";}
        else if(phase==1){if(++ticks>=2){p.getInventory().setSelectedSlot(activeMaceSlot);LivingEntity t=findTarget(p,mc,6);if(t!=null)aimAt(p,t);attackTarget(p,mc,t);phase=2;ticks=0;}}
        else if(++ticks>=10){phase=0;status="RETRYING";}
    }
    private static void tickCritChain(LocalPlayer p,Minecraft mc){
        if(phase==0&&p.onGround()){activeSwordSlot=findAndEquip(p,mc,"_sword",swordSlot);if(activeSwordSlot<0){missing("SWORD");return;}p.getInventory().setSelectedSlot(activeSwordSlot);mc.options.keyJump.setDown(true);phase=1;status="JUMPING";}
        else if(phase==1){mc.options.keyJump.setDown(false);phase=2;}
        else if(phase==2&&p.getDeltaMovement().y < -0.08){LivingEntity t=findTarget(p,mc,6);if(t!=null)aimAt(p,t);attackTarget(p,mc,t);phase=3;status="CRIT";}
        else if(phase==3&&p.onGround()){phase=0;status="RETRYING";}
    }

    /** Elytra mace: launch, firework boost, dive, unequip before the falling mace hit. */
    private static void tickElytraMace(LocalPlayer p,Minecraft mc){
        switch(phase){
            case 0 -> {
                String chestPath=BuiltInRegistries.ITEM.getKey(p.getItemBySlot(EquipmentSlot.CHEST).getItem()).getPath();
                elytraWasAlreadyWorn=chestPath.equals("elytra");
                if(elytraWasAlreadyWorn){
                    elytraStashSlot=findEmptyInventorySlot(p);
                    if(elytraStashSlot<0){missing("EMPTY INVENTORY SLOT");return;}
                }else{
                    elytraInventorySlot=findInventorySlot(p,"elytra");
                    if(elytraInventorySlot<0){missing("ELYTRA");return;}
                    equipChestItem(p,mc,elytraInventorySlot);
                    elytraEquippedByMacro=true;
                }
                int macePreferred=safeHotbarSlot(maceSlot,elytraInventorySlot);
                activeMaceSlot=findAndEquip(p,mc,"mace",macePreferred);
                if(activeMaceSlot<0){missing("MACE");return;}
                int rocketPreferred=safeHotbarSlot(6,elytraInventorySlot);
                if(rocketPreferred==activeMaceSlot)rocketPreferred=safeHotbarSlot(5,elytraInventorySlot);
                rocketSlot=findAndEquip(p,mc,"firework_rocket",rocketPreferred);
                if(rocketSlot<0){missing("FIREWORK ROCKET");return;}
                phase=1;ticks=0;timeout=0;status="ELYTRA READY - JUMP";
            }
            case 1 -> {
                if(p.onGround()){
                    mc.options.keyJump.setDown(true);
                    if(++ticks>=2){mc.options.keyJump.setDown(false);ticks=0;phase=2;status="WAITING TO FALL";}
                }else if(p.getDeltaMovement().y<0){
                    mc.options.keyJump.setDown(true);phase=3;timeout=0;status="DEPLOYING ELYTRA";
                }else if(++timeout>100){missing("JUMP FROM A LEDGE");}
            }
            case 2 -> {
                mc.options.keyJump.setDown(false);
                if(p.getDeltaMovement().y<0){mc.options.keyJump.setDown(true);phase=3;timeout=0;status="DEPLOYING ELYTRA";}
                else if(++timeout>100){missing("JUMP FROM A LEDGE");}
            }
            case 3 -> {
                mc.options.keyJump.setDown(false);
                if(p.isFallFlying()){
                    p.getInventory().setSelectedSlot(rocketSlot);
                    mc.options.keyUp.setDown(true);
                    mc.gameMode.useItem(p,InteractionHand.MAIN_HAND);
                    phase=4;ticks=0;timeout=0;status="FIREWORK BOOST";
                }else if(++timeout>40){phase=1;timeout=0;status="ELYTRA NOT DEPLOYED - RETRY";}
            }
            case 4 -> {
                mc.options.keyUp.setDown(true);
                LivingEntity target=findTarget(p,mc,32);
                if(target!=null)aimAt(p,target);else p.setXRot(8.0F);
                if(++ticks>=24){phase=5;ticks=0;timeout=0;status="DIVING - TRACKING TARGET";}
            }
            case 5 -> {
                mc.options.keyUp.setDown(true);
                LivingEntity target=findTarget(p,mc,32);
                if(target!=null){
                    aimAt(p,target);
                    p.setXRot(Math.max(18.0F,p.getXRot()));
                }else p.setXRot(55.0F);
                // Stop gliding while there is still room to build a real falling distance.
                if((target!=null&&p.distanceTo(target)<=12.0F)||++timeout>=45){
                    mc.options.keyUp.setDown(false);
                    if(elytraEquippedByMacro){
                        unequipElytra(p,mc);
                        elytraEquippedByMacro=false;
                    }else if(elytraWasAlreadyWorn){
                        stashWornElytra(p,mc);
                    }
                    p.getInventory().setSelectedSlot(activeMaceSlot);
                    diveStartY=p.getY();
                    phase=6;timeout=0;status="ELYTRA OFF - FALLING FOR MACE";
                }
            }
            case 6 -> {
                mc.options.keyUp.setDown(false);
                LivingEntity target=findTarget(p,mc,12);
                if(target!=null)aimAt(p,target);
                double fallen=diveStartY-p.getY();
                if(target!=null&&fallen>=1.6&&p.getDeltaMovement().y< -0.08&&p.distanceTo(target)<=4.5F){
                    attackTarget(p,mc,target);
                    phase=7;timeout=0;status="MACE STRIKE";
                }else if(p.onGround()){
                    phase=7;timeout=0;status="LANDED - NO SMASH HIT";
                }else if(++timeout>100){
                    phase=7;timeout=0;status="DIVE MISSED";
                }
            }
            case 7 -> {
                if(p.onGround()){
                    if(elytraWasAlreadyWorn&&elytraStashSlot>=0)restoreStashedElytra(p,mc);
                    phase=0;ticks=0;timeout=0;status="LANDED - READY FOR NEXT RUN";
                }
            }
        }
    }

    private static int safeHotbarSlot(int preferred,int reservedInventorySlot){
        int slot=wrap(preferred);
        if(reservedInventorySlot>=0&&reservedInventorySlot<9&&slot==reservedInventorySlot)slot=wrap(slot+1);
        return slot;
    }
    private static int findEmptyInventorySlot(LocalPlayer p){
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).isEmpty())return i;
        return -1;
    }
    private static void stashWornElytra(LocalPlayer p,Minecraft mc){
        if(elytraStashSlot<0)return;
        int chest=6,stash=menuSlotForInventoryIndex(elytraStashSlot);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,chest,0,ClickType.PICKUP,p);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,stash,0,ClickType.PICKUP,p);
    }
    private static void restoreStashedElytra(LocalPlayer p,Minecraft mc){
        if(elytraStashSlot<0)return;
        int chest=6,stash=menuSlotForInventoryIndex(elytraStashSlot);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,stash,0,ClickType.PICKUP,p);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,chest,0,ClickType.PICKUP,p);
        elytraStashSlot=-1;elytraWasAlreadyWorn=false;
    }
    private static void unequipElytra(LocalPlayer p,Minecraft mc){
        if(elytraInventorySlot<0)return;
        int source=menuSlotForInventoryIndex(elytraInventorySlot),chest=6;
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,chest,0,ClickType.PICKUP,p);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,source,0,ClickType.PICKUP,p);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,chest,0,ClickType.PICKUP,p);
    }

    private static int findInventorySlot(LocalPlayer p,String path){
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            var stack=p.getInventory().getItem(i);
            if(!stack.isEmpty()&&BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals(path))return i;
        }
        return -1;
    }
    private static int menuSlotForInventoryIndex(int index){return index<9?36+index:index;}
    private static void equipChestItem(LocalPlayer p,Minecraft mc,int inventoryIndex){
        if(BuiltInRegistries.ITEM.getKey(p.getItemBySlot(EquipmentSlot.CHEST).getItem()).getPath().equals("elytra"))return;
        int source=menuSlotForInventoryIndex(inventoryIndex),chest=6;
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,source,0,ClickType.PICKUP,p);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,chest,0,ClickType.PICKUP,p);
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,source,0,ClickType.PICKUP,p);
    }

    /** Searches all player inventory slots and swaps a match into the preferred hotbar slot. */
    private static int findAndEquip(LocalPlayer p,Minecraft mc,String itemPath,int preferred){
        int found=-1;
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            var stack=p.getInventory().getItem(i);if(stack.isEmpty())continue;
            String path=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            if(itemPath.startsWith("_")?path.endsWith(itemPath):path.equals(itemPath)){found=i;break;}
        }
        if(found<0)return -1;
        if(found<9)return found;
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,found,preferred,ClickType.SWAP,p);
        return preferred;
    }
    private static LivingEntity findTarget(LocalPlayer p,Minecraft mc,double range){
        AABB box=p.getBoundingBox().inflate(range);
        return mc.level.getEntitiesOfClass(LivingEntity.class,box,e->e!=p&&e.isAlive()&&!e.isSpectator()&&p.distanceTo(e)<=range)
                .stream().min((a,b)->Double.compare(p.distanceTo(a),p.distanceTo(b))).orElse(null);
    }
    private static void aimAt(LocalPlayer p,LivingEntity t){
        double dx=t.getX()-p.getX(),dz=t.getZ()-p.getZ(),dy=t.getEyeY()-p.getEyeY();
        double horizontal=Math.sqrt(dx*dx+dz*dz);
        p.setYRot((float)(Math.toDegrees(Math.atan2(dz,dx))-90.0));
        p.setXRot((float)-Math.toDegrees(Math.atan2(dy,horizontal)));
    }
    private static void attackTarget(LocalPlayer p,Minecraft mc,LivingEntity t){
        if(t!=null&&t.isAlive()&&p.distanceTo(t)<=6.0F){mc.gameMode.attack(p,t);p.swing(InteractionHand.MAIN_HAND);}
    }
    private static void missing(String item){stop();status="MISSING "+item;}
}
