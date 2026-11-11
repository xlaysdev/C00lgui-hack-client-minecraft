package com.xlays.c00lgui.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.AABB;

/** Inventory-aware, user-started PvP sequences with automatic target tracking. */
public final class MacroManager {
    public enum Macro { WIND_CHARGE_MACE, SWORD_MACE, SWORD_WTAP, AXE_STUN_MACE, CRIT_CHAIN }

    private static Macro active;
    private static int phase, ticks, timeout;
    private static int chargeSlot=7, maceSlot=8, swordSlot=0, axeSlot=1;
    private static int activeChargeSlot=-1, activeMaceSlot=-1, activeSwordSlot=-1, activeAxeSlot=-1;
    private static float savedPitch, savedYaw;
    private static boolean sprintWasActive;
    private static String status="READY";

    private MacroManager() {}

    public static boolean isRunning(){ return active!=null; }
    public static Macro getActive(){ return active; }
    public static String getStatus(){ return status; }
    public static int getChargeSlot(){ return chargeSlot+1; }
    public static int getMaceSlot(){ return maceSlot+1; }
    public static int getSwordSlot(){ return swordSlot+1; }
    public static int getAxeSlot(){ return axeSlot+1; }
    public static void cycleChargeSlot(int delta){chargeSlot=wrap(chargeSlot+delta);}
    public static void cycleMaceSlot(int delta){maceSlot=wrap(maceSlot+delta);}
    public static void cycleSwordSlot(int delta){swordSlot=wrap(swordSlot+delta);}
    public static void cycleAxeSlot(int delta){axeSlot=wrap(axeSlot+delta);}
    private static int wrap(int slot){return Math.floorMod(slot,9);}

    public static void start(Macro macro){
        stop();
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null){status="JOIN A WORLD FIRST";return;}
        active=macro;phase=0;ticks=0;timeout=0;
        savedYaw=mc.player.getYRot();savedPitch=mc.player.getXRot();
        sprintWasActive=mc.player.isSprinting();
        status="RUNNING";
    }

    public static void stop(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player!=null){
            if(active!=null){mc.player.setYRot(savedYaw);mc.player.setXRot(savedPitch);}
            if(active==Macro.SWORD_WTAP||active==Macro.AXE_STUN_MACE||active==Macro.CRIT_CHAIN)
                mc.player.setSprinting(sprintWasActive);
        }
        mc.options.keyJump.setDown(false);
        active=null;phase=0;ticks=0;timeout=0;
        activeChargeSlot=activeMaceSlot=activeSwordSlot=activeAxeSlot=-1;
        if(!status.startsWith("MISSING"))status="READY";
    }
    public static void toggle(Macro macro){if(active==macro)stop();else start(macro);}

    public static void tick(){
        if(active==null)return;
        Minecraft mc=Minecraft.getInstance();
        LocalPlayer player=mc.player;
        if(player==null||mc.level==null||mc.gameMode==null||
                (mc.screen!=null && !(mc.screen instanceof com.xlays.c00lgui.ClickGuiScreen))){
            stop();return;
        }
        switch(active){
            case WIND_CHARGE_MACE -> tickWindCharge(player,mc);
            case SWORD_MACE -> tickSwordMace(player,mc);
            case SWORD_WTAP -> tickWTap(player,mc);
            case AXE_STUN_MACE -> tickAxeStun(player,mc);
            case CRIT_CHAIN -> tickCritChain(player,mc);
        }
    }

    private static void tickWindCharge(LocalPlayer player,Minecraft mc){
        switch(phase){
            case 0 -> {
                activeChargeSlot=selectItem(player,mc,"wind_charge",chargeSlot);
                if(activeChargeSlot<0){missing("WIND CHARGE");return;}
                activeMaceSlot=findAndEquip(player,mc,"mace",maceSlot);
                if(activeMaceSlot<0){missing("MACE");return;}
                player.getInventory().setSelectedSlot(activeChargeSlot);
                player.setXRot(88.0F);
                phase=1;ticks=0;
            }
            case 1 -> {
                if(++ticks>=2){
                    mc.gameMode.useItem(player,InteractionHand.MAIN_HAND);
                    player.setXRot(savedPitch);
                    phase=2;timeout=0;
                }
            }
            case 2 -> {
                LivingEntity target=findTarget(player,mc,8.0);
                if(target!=null)aimAt(player,target);
                if(player.getDeltaMovement().y>0.12){phase=3;timeout=0;}
                else if(++timeout>45){status="NO LAUNCH - CHECK WIND CHARGE";phase=0;timeout=0;}
            }
            case 3 -> {
                player.getInventory().setSelectedSlot(activeMaceSlot);
                LivingEntity target=findTarget(player,mc,8.0);
                if(target!=null)aimAt(player,target);
                if(player.getDeltaMovement().y < -0.08){
                    attackTarget(player,mc,target);
                    phase=4;ticks=0;
                }
            }
            case 4 -> {if(++ticks>=12){phase=0;ticks=0;}}
        }
    }

    private static void tickSwordMace(LocalPlayer player,Minecraft mc){
        switch(phase){
            case 0 -> {
                activeSwordSlot=findAndEquip(player,mc,"_sword",swordSlot);
                activeMaceSlot=findAndEquip(player,mc,"mace",maceSlot);
                if(activeSwordSlot<0){missing("SWORD");return;}
                if(activeMaceSlot<0){missing("MACE");return;}
                player.getInventory().setSelectedSlot(activeSwordSlot);
                phase=1;ticks=0;
            }
            case 1 -> {
                LivingEntity target=findTarget(player,mc,6.0);
                if(target!=null)aimAt(player,target);
                if(++ticks>=2){attackTarget(player,mc,target);player.getInventory().setSelectedSlot(activeMaceSlot);phase=2;ticks=0;}
            }
            case 2 -> {
                LivingEntity target=findTarget(player,mc,6.0);
                if(target!=null)aimAt(player,target);
                if(++ticks>=3){attackTarget(player,mc,target);phase=3;ticks=0;}
            }
            case 3 -> {if(++ticks>=10){phase=0;ticks=0;}}
        }
    }

    private static void tickWTap(LocalPlayer player,Minecraft mc){
        if(phase==0){
            activeSwordSlot=findAndEquip(player,mc,"_sword",swordSlot);
            if(activeSwordSlot<0){missing("SWORD");return;}
            player.getInventory().setSelectedSlot(activeSwordSlot);
            LivingEntity target=findTarget(player,mc,6.0);
            if(target!=null)aimAt(player,target);
            player.setSprinting(true);attackTarget(player,mc,target);player.setSprinting(false);
            phase=1;ticks=0;
        }else if(phase==1){if(++ticks>=1){player.setSprinting(true);phase=2;ticks=0;}}
        else if(++ticks>=7)phase=0;
    }

    private static void tickAxeStun(LocalPlayer player,Minecraft mc){
        if(phase==0){
            activeAxeSlot=findAndEquip(player,mc,"_axe",axeSlot);
            activeMaceSlot=findAndEquip(player,mc,"mace",maceSlot);
            if(activeAxeSlot<0){missing("AXE");return;}
            if(activeMaceSlot<0){missing("MACE");return;}
            player.getInventory().setSelectedSlot(activeAxeSlot);
            LivingEntity target=findTarget(player,mc,6.0);
            if(target!=null)aimAt(player,target);
            attackTarget(player,mc,target);phase=1;ticks=0;
        }else if(phase==1){
            if(++ticks>=2){
                player.getInventory().setSelectedSlot(activeMaceSlot);
                LivingEntity target=findTarget(player,mc,6.0);
                if(target!=null)aimAt(player,target);
                attackTarget(player,mc,target);phase=2;ticks=0;
            }
        }else if(++ticks>=10)phase=0;
    }

    private static void tickCritChain(LocalPlayer player,Minecraft mc){
        if(phase==0&&player.onGround()){
            activeSwordSlot=findAndEquip(player,mc,"_sword",swordSlot);
            if(activeSwordSlot<0){missing("SWORD");return;}
            player.getInventory().setSelectedSlot(activeSwordSlot);
            mc.options.keyJump.setDown(true);phase=1;return;
        }
        if(phase==1){mc.options.keyJump.setDown(false);phase=2;return;}
        if(phase==2&&player.getDeltaMovement().y < -0.08){
            LivingEntity target=findTarget(player,mc,6.0);
            if(target!=null)aimAt(player,target);
            attackTarget(player,mc,target);phase=3;return;
        }
        if(phase==3&&player.onGround())phase=0;
    }

    /** Finds a matching item anywhere in the 36-slot player inventory and swaps it into the requested hotbar slot. */
    private static int findAndEquip(LocalPlayer player,Minecraft mc,String itemPath,int preferredHotbar){
        int found=-1;
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            var stack=player.getInventory().getItem(i);
            if(stack.isEmpty())continue;
            String path=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            boolean match=itemPath.startsWith("_")?path.endsWith(itemPath):path.equals(itemPath);
            if(match){found=i;break;}
        }
        if(found<0)return -1;
        if(found<9)return found;
        // Player inventory menu slots 9-35 map directly to inventory slots 9-35.
        mc.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId,found,preferredHotbar,ClickType.SWAP,player);
        return preferredHotbar;
    }

    private static int selectItem(LocalPlayer player,Minecraft mc,String itemPath,int preferredHotbar){
        return findAndEquip(player,mc,itemPath,preferredHotbar);
    }

    private static LivingEntity findTarget(LocalPlayer player,Minecraft mc,double range){
        AABB area=player.getBoundingBox().inflate(range);
        return mc.level.getEntitiesOfClass(LivingEntity.class,area,e->
                e!=player&&e.isAlive()&&!e.isSpectator()&&player.distanceTo(e)<=range)
                .stream().min((a,b)->Double.compare(player.distanceTo(a),player.distanceTo(b)))
                .orElse(null);
    }

    private static void aimAt(LocalPlayer player,LivingEntity target){
        double dx=target.getX()-player.getX();
        double dz=target.getZ()-player.getZ();
        double dy=target.getEyeY()-player.getEyeY();
        double horizontal=Math.sqrt(dx*dx+dz*dz);
        float yaw=(float)(Math.toDegrees(Math.atan2(dz,dx))-90.0);
        float pitch=(float)-Math.toDegrees(Math.atan2(dy,horizontal));
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    private static void attackTarget(LocalPlayer player,Minecraft mc,LivingEntity target){
        if(target!=null&&target.isAlive()&&player.distanceTo(target)<=6.0F){
            mc.gameMode.attack(player,target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static void missing(String item){
        status="MISSING "+item+" - STOPPED";
        stop();
        status="MISSING "+item;
    }
}
