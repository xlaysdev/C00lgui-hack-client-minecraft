package com.xlays.c00lgui.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Tick-driven, user-started combat sequences. */
public final class MacroManager {
    public enum Macro { WIND_CHARGE_MACE, SWORD_MACE, SWORD_WTAP, AXE_STUN_MACE, CRIT_CHAIN }
    private static Macro active;
    private static int phase, ticks, timeout;
    private static float savedPitch;
    private static int chargeSlot=7, maceSlot=8, swordSlot=0, axeSlot=1;
    private static boolean sprintWasActive;

    private MacroManager() {}

    public static boolean isRunning(){ return active!=null; }
    public static Macro getActive(){ return active; }
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
        active=macro;phase=0;ticks=0;timeout=0;
        Minecraft mc=Minecraft.getInstance();sprintWasActive=mc.player!=null&&mc.player.isSprinting();
    }
    public static void stop(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player!=null && active==Macro.WIND_CHARGE_MACE && phase==1)
            mc.player.setXRot(savedPitch);
        if(mc.player!=null && (active==Macro.SWORD_WTAP || active==Macro.AXE_STUN_MACE || active==Macro.CRIT_CHAIN)) mc.player.setSprinting(sprintWasActive);
        mc.options.keyJump.setDown(false);
        active=null;phase=0;ticks=0;timeout=0;
    }
    public static void toggle(Macro macro){if(active==macro)stop();else start(macro);}

    public static void tick(){
        if(active==null)return;
        Minecraft mc=Minecraft.getInstance();
        LocalPlayer player=mc.player;
        if(player==null||mc.level==null||mc.gameMode==null||(mc.screen!=null && !(mc.screen instanceof com.xlays.c00lgui.ClickGuiScreen))){stop();return;}
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
                player.getInventory().setSelectedSlot(chargeSlot);
                savedPitch=player.getXRot();
                player.setXRot(88.0F);
                phase=1;ticks=0;
            }
            case 1 -> {
                if(++ticks>=2){
                    mc.gameMode.useItem(player,InteractionHand.MAIN_HAND);
                    player.setXRot(savedPitch);
                    phase=2;ticks=0;timeout=0;
                }
            }
            case 2 -> {
                // Wait until the downward-fired charge has launched the player upward.
                if(player.getDeltaMovement().y>0.12){phase=3;timeout=0;}
                else if(++timeout>35){phase=0;}
            }
            case 3 -> {
                // Keep the mace selected for the falling hit; the player still aims manually.
                player.getInventory().setSelectedSlot(maceSlot);
                if(player.getDeltaMovement().y < -0.08){
                    attackCrosshair(player,mc);
                    phase=4;ticks=0;
                }
            }
            case 4 -> {if(++ticks>=12){phase=0;ticks=0;}}
        }
    }

    private static void tickSwordMace(LocalPlayer player,Minecraft mc){
        switch(phase){
            case 0 -> {player.getInventory().setSelectedSlot(swordSlot);phase=1;ticks=0;}
            case 1 -> {
                if(++ticks>=1){
                    attackCrosshair(player,mc);
                    player.getInventory().setSelectedSlot(maceSlot);
                    phase=2;ticks=0;
                }
            }
            case 2 -> {
                if(++ticks>=3){
                    attackCrosshair(player,mc);
                    phase=3;ticks=0;
                }
            }
            case 3 -> {if(++ticks>=8){phase=0;ticks=0;}}
        }
    }


    /** Sprint-reset combo: hit with sword, briefly release sprint, then re-engage. */
    private static void tickWTap(LocalPlayer player,Minecraft mc){
        switch(phase){
            case 0 -> {player.getInventory().setSelectedSlot(swordSlot);player.setSprinting(true);attackCrosshair(player,mc);player.setSprinting(false);phase=1;ticks=0;}
            case 1 -> {if(++ticks>=1){player.setSprinting(true);phase=2;ticks=0;}}
            case 2 -> {if(++ticks>=7)phase=0;}
        }
    }

    /** Axe shield-disable attempt followed by a mace follow-up; aim stays manual. */
    private static void tickAxeStun(LocalPlayer player,Minecraft mc){
        switch(phase){
            case 0 -> {player.getInventory().setSelectedSlot(axeSlot);attackCrosshair(player,mc);phase=1;ticks=0;}
            case 1 -> {if(++ticks>=2){player.getInventory().setSelectedSlot(maceSlot);attackCrosshair(player,mc);phase=2;ticks=0;}}
            case 2 -> {if(++ticks>=10)phase=0;}
        }
    }

    /** Critical chain: jumps when grounded and attacks on the falling portion of each jump. */
    private static void tickCritChain(LocalPlayer player,Minecraft mc){
        if(phase==0 && player.onGround()){
            player.getInventory().setSelectedSlot(swordSlot);
            mc.options.keyJump.setDown(true);
            phase=1;
            return;
        }
        if(phase==1){
            mc.options.keyJump.setDown(false);
            phase=2;
            return;
        }
        if(phase==2 && player.getDeltaMovement().y < -0.08){
            attackCrosshair(player,mc);
            phase=3;
            return;
        }
        if(phase==3 && player.onGround())phase=0;
    }

    private static void attackCrosshair(LocalPlayer player,Minecraft mc){
        if(!(mc.hitResult instanceof EntityHitResult hit))return;
        Entity target=hit.getEntity();
        if(target instanceof LivingEntity && target.isAlive() && player.distanceTo(target)<=6.0F){
            mc.gameMode.attack(player,target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }
}