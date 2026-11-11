package com.xlays.c00lgui.modules.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

public final class OreESP {
    public enum OreGroup {
        COAL("Coal",Blocks.COAL_ORE,Blocks.COAL_ORE,Blocks.DEEPSLATE_COAL_ORE),
        IRON("Iron",Blocks.IRON_ORE,Blocks.IRON_ORE,Blocks.DEEPSLATE_IRON_ORE),
        COPPER("Copper",Blocks.COPPER_ORE,Blocks.COPPER_ORE,Blocks.DEEPSLATE_COPPER_ORE),
        GOLD("Gold",Blocks.GOLD_ORE,Blocks.GOLD_ORE,Blocks.DEEPSLATE_GOLD_ORE),
        REDSTONE("Redstone",Blocks.REDSTONE_ORE,Blocks.REDSTONE_ORE,Blocks.DEEPSLATE_REDSTONE_ORE),
        LAPIS("Lapis",Blocks.LAPIS_ORE,Blocks.LAPIS_ORE,Blocks.DEEPSLATE_LAPIS_ORE),
        DIAMOND("Diamond",Blocks.DIAMOND_ORE,Blocks.DIAMOND_ORE,Blocks.DEEPSLATE_DIAMOND_ORE),
        EMERALD("Emerald",Blocks.EMERALD_ORE,Blocks.EMERALD_ORE,Blocks.DEEPSLATE_EMERALD_ORE),
        ANCIENT_DEBRIS("Ancient Debris",Blocks.ANCIENT_DEBRIS,Blocks.ANCIENT_DEBRIS);
        private final String label; private final Block icon; private final List<Block> blocks;
        OreGroup(String label,Block icon,Block... blocks){this.label=label;this.icon=icon;this.blocks=List.of(blocks);}
        public String getLabel(){return label;} public Block getIcon(){return icon;} public List<Block> getBlocks(){return blocks;}
    }
    private static boolean enabled=false;
    private static int radius=12,tickCounter=0;
    private static final List<BlockPos> ORES=new ArrayList<>();
    private static final EnumMap<OreGroup,Boolean> FILTERS=new EnumMap<>(OreGroup.class);
    private static final Set<Block> ACTIVE_BLOCKS=new HashSet<>();
    static { for(OreGroup group:OreGroup.values()) FILTERS.put(group,true); rebuildTargets(); }
    private OreESP(){}
    public static boolean isEnabled(){return enabled;}
    public static void setEnabled(boolean value){enabled=value;if(!value){ORES.clear();tickCounter=0;}}
    public static void toggle(){setEnabled(!enabled);}
    public static int getRadius(){return radius;}
    public static void setRadius(int value){radius=Math.max(4,Math.min(32,value));tickCounter=9;}
    public static boolean isOreEnabled(OreGroup group){return FILTERS.getOrDefault(group,true);}
    public static void setOreEnabled(OreGroup group,boolean value){FILTERS.put(group,value);rebuildTargets();ORES.clear();tickCounter=9;}
    public static void toggleOre(OreGroup group){setOreEnabled(group,!isOreEnabled(group));}
    private static void rebuildTargets(){ACTIVE_BLOCKS.clear();for(OreGroup group:OreGroup.values())if(isOreEnabled(group))ACTIVE_BLOCKS.addAll(group.blocks);}
    public static void tick(){
        if(!enabled){ORES.clear();return;}
        Minecraft mc=Minecraft.getInstance();LocalPlayer player=mc.player;
        if(player==null||mc.level==null){ORES.clear();return;}
        if(ACTIVE_BLOCKS.isEmpty()){ORES.clear();return;}
        if(++tickCounter<10)return;tickCounter=0;
        BlockPos center=player.blockPosition();int r2=radius*radius;
        List<BlockPos> found=new ArrayList<>();
        // Sphere scan visits fewer blocks than the old cube; squared distance avoids sqrt per block.
        for(int x=-radius;x<=radius;x++)for(int y=-radius;y<=radius;y++)for(int z=-radius;z<=radius;z++){
            if(x*x+y*y+z*z>r2)continue;
            BlockPos pos=center.offset(x,y,z);
            if(!mc.level.hasChunkAt(pos))continue;
            if(ACTIVE_BLOCKS.contains(mc.level.getBlockState(pos).getBlock()))found.add(pos.immutable());
        }
        ORES.clear();ORES.addAll(found);
    }
    public static void render(){
        if(!enabled||ORES.isEmpty())return;
        Minecraft mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        for(BlockPos pos:ORES){
            Block block=mc.level.getBlockState(pos).getBlock();
            if(ACTIVE_BLOCKS.contains(block))Gizmos.cuboid(pos,GizmoStyle.stroke(getColor(block),2.0F)).setAlwaysOnTop();
        }
    }
    public static int getColor(Block block){
        if(block==Blocks.DIAMOND_ORE||block==Blocks.DEEPSLATE_DIAMOND_ORE)return 0xFF55FFFF;
        if(block==Blocks.EMERALD_ORE||block==Blocks.DEEPSLATE_EMERALD_ORE)return 0xFF55FF55;
        if(block==Blocks.GOLD_ORE||block==Blocks.DEEPSLATE_GOLD_ORE)return 0xFFFFD700;
        if(block==Blocks.IRON_ORE||block==Blocks.DEEPSLATE_IRON_ORE)return 0xFFFFAA66;
        if(block==Blocks.COPPER_ORE||block==Blocks.DEEPSLATE_COPPER_ORE)return 0xFFFF8844;
        if(block==Blocks.REDSTONE_ORE||block==Blocks.DEEPSLATE_REDSTONE_ORE)return 0xFFFF2222;
        if(block==Blocks.LAPIS_ORE||block==Blocks.DEEPSLATE_LAPIS_ORE)return 0xFF4444FF;
        if(block==Blocks.COAL_ORE||block==Blocks.DEEPSLATE_COAL_ORE)return 0xFF777777;
        if(block==Blocks.ANCIENT_DEBRIS)return 0xFFAA55AA;
        return 0xFFFFFFFF;
    }
    public static void disable(){setEnabled(false);}
}