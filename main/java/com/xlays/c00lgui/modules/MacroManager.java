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
    public enum Macro { WIND_CHARGE_MACE, SWORD_MACE }
    private static Macro active;
    private static int phase, ticks, timeout;
    private static float savedPitch;
    private static int chargeSlot=7, maceSlot=8, swordSlot=0;

    private MacroManager() {}

    public static boolean isRunning(){ return active!=null; }
    public static Macro getActive(){ return active; }
    public static int getChargeSlot(){ return chargeSlot+1; }
    public static int getMaceSlot(){ return maceSlot+1; }
    public static int getSwordSlot(){ return swordSlot+1; }
    public static void cycleChargeSlot(int delta){chargeSlot=wrap(chargeSlot+delta);}
    public static void cycleMaceSlot(int delta){maceSlot=wrap(maceSlot+delta);}
    public static void cycleSwordSlot(int delta){swordSlot=wrap(swordSlot+delta);}
    private static int wrap(int slot){return Math.floorMod(slot,9);}

    public static void start(Macro macro){
        stop();
        active=macro;phase=0;ticks=0;timeout=0;
    }
    public static void stop(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player!=null && active==Macro.WIND_CHARGE_MACE && phase==1)
            mc.player.setXRot(savedPitch);
        active=null;phase=0;ticks=0;timeout=0;
    }
    public static void toggle(Macro macro){if(active==macro)stop();else start(macro);}

    public static void tick(){
        if(active==null)return;
        Minecraft mc=Minecraft.getInstance();
        LocalPlayer player=mc.player;
        if(player==null||mc.level==null||mc.gameMode==null||mc.screen!=null){stop();return;}
        switch(active){
            case WIND_CHARGE_MACE -> tickWindCharge(player,mc);
            case SWORD_MACE -> tickSwordMace(player,mc);
        }
    }

    private static void tickWindCharge(LocalPlayer player,Minecraft mc){
        switch(phase){
            case 0 -> {
                player.getInventory().setSelectedSlot(chargeSlot;
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
                player.getInventory().setSelectedSlot(maceSlot;
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
            case 0 -> {player.getInventory().setSelectedSlot(swordSlot;phase=1;ticks=0;}
            case 1 -> {
                if(++ticks>=1){
                    attackCrosshair(player,mc);
                    player.getInventory().setSelectedSlot(maceSlot;
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

    private static void attackCrosshair(LocalPlayer player,Minecraft mc){
        if(!(mc.hitResult instanceof EntityHitResult hit))return;
        Entity target=hit.getEntity();
        if(target instanceof LivingEntity && target.isAlive() && player.distanceTo(target)<=6.0F){
            mc.gameMode.attack(player,target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }
}