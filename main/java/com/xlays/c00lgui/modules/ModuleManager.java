package com.xlays.c00lgui.modules;

import com.xlays.c00lgui.modules.combat.*;
import com.xlays.c00lgui.modules.movement.*;
import com.xlays.c00lgui.modules.player.*;
import com.xlays.c00lgui.modules.visual.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {

    private static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, List<ModuleSetting>> SETTINGS = new LinkedHashMap<>();

    private static final List<String> HIDDEN = List.of(
            "AutoCritical",
            "OreESP",
            "PlayerESP"
    );

    private static final int[] COLOR_PRESETS = {
            0xFFFFAA00, // orange
            0xFFFF3333, // red
            0xFF33FF33, // green
            0xFF3399FF, // blue
            0xFFFFFF33, // yellow
            0xFF33FFFF, // cyan
            0xFFFF33FF, // magenta
            0xFFFFFFFF  // white
    };

    static {
        register("Speed", "MOVEMENT");
        register("Fly", "MOVEMENT");
        register("AirJump", "MOVEMENT");
        register("Jesus", "MOVEMENT");
        register("Step", "MOVEMENT");
        register("NoFall", "MOVEMENT");

        register("Reach", "COMBAT");
        register("AutoClicker", "COMBAT");
        register("AimAssist", "COMBAT");
        register("KillAura", "COMBAT");
        register("AutoCritical", "COMBAT");

        register("FastBreak", "PLAYER");
        register("FastPlace", "PLAYER");
        register("Scaffold", "PLAYER");

        register("OreESP", "VISUAL");
        register("PlayerESP", "VISUAL");
        register("FullBright", "VISUAL");
        register("Tracers", "VISUAL");
        register("ESP", "VISUAL");
        register("EntityHighlight", "VISUAL");
        register("CriticalHUD", "VISUAL");

        registerAllSettings();
    }

    private ModuleManager() {}

    private static void register(String name, String category) {
        MODULES.put(name, false);
        CATEGORIES.put(name, category);
        SETTINGS.put(name, new ArrayList<>());
    }

    private static void registerAllSettings() {

        // Fly
        setting("Fly", "Horizontal Speed", ModuleSetting.Type.DOUBLE, 0.1, 10.0,
                Fly::getHorizontalSpeed, v -> Fly.setHorizontalSpeed((Double) v),
                "Horizontal flight speed");
        setting("Fly", "Vertical Speed", ModuleSetting.Type.DOUBLE, 0.1, 10.0,
                Fly::getVerticalSpeed, v -> Fly.setVerticalSpeed((Double) v),
                "Vertical flight speed");
        setting("Fly", "Prevent Fall Damage", ModuleSetting.Type.BOOL, 0, 1,
                Fly::isPreventFallDamage, v -> Fly.setPreventFallDamage((Boolean) v),
                "Reset fall distance while flying");

        // Speed
        setting("Speed", "Blocks Per Second", ModuleSetting.Type.DOUBLE, 5.0, 15.0,
                Speed::getBlocksPerSecond, v -> Speed.setBlocksPerSecond((Double) v),
                "Movement speed in blocks per second");

        // NoFall
        setting("NoFall", "Min Fall Distance", ModuleSetting.Type.DOUBLE, 0.0, 10.0,
                NoFall::getMinFallDistance, v -> NoFall.setMinFallDistance((Double) v),
                "Fall distance before cancellation");

        // Step
        setting("Step", "Step Height", ModuleSetting.Type.DOUBLE, 0.6, 2.5,
                Step::getStepHeight, v -> Step.setStepHeight((Double) v),
                "Maximum step-up height");

        // Reach
        setting("Reach", "Reach Distance", ModuleSetting.Type.DOUBLE, 3.0, 8.0,
                Reach::getReachDistance, v -> Reach.setReachDistance((Double) v),
                "Attack reach distance");

        // AutoClicker
        setting("AutoClicker", "CPS", ModuleSetting.Type.INT, 1, 20,
                AutoClicker::getCps, v -> AutoClicker.setCps(((Number) v).intValue()),
                "Clicks per second");
        setting("AutoClicker", "Left Click", ModuleSetting.Type.BOOL, 0, 1,
                AutoClicker::isLeftClick, v -> AutoClicker.setLeftClick((Boolean) v),
                "Auto-click left mouse");
        setting("AutoClicker", "Right Click", ModuleSetting.Type.BOOL, 0, 1,
                AutoClicker::isRightClick, v -> AutoClicker.setRightClick((Boolean) v),
                "Auto-click right mouse");
        setting("AutoClicker", "Only Holding Sword", ModuleSetting.Type.BOOL, 0, 1,
                AutoClicker::isOnlyHoldingSword, v -> AutoClicker.setOnlyHoldingSword((Boolean) v),
                "Only while holding a sword");
        setting("AutoClicker", "Only Holding Tool", ModuleSetting.Type.BOOL, 0, 1,
                AutoClicker::isOnlyHoldingTool, v -> AutoClicker.setOnlyHoldingTool((Boolean) v),
                "Only while holding a tool");

        // AimAssist
        setting("AimAssist", "Range", ModuleSetting.Type.DOUBLE, 2.0, 10.0,
                AimAssist::getRange, v -> AimAssist.setRange((Double) v),
                "Aim search range");
        setting("AimAssist", "Speed", ModuleSetting.Type.DOUBLE, 0.5, 10.0,
                AimAssist::getSpeed, v -> AimAssist.setSpeed(((Number) v).floatValue()),
                "Aim snap speed");
        setting("AimAssist", "Only When Attacking", ModuleSetting.Type.BOOL, 0, 1,
                AimAssist::isOnlyWhenAttacking, v -> AimAssist.setOnlyWhenAttacking((Boolean) v),
                "Only aim while attacking");
        setting("AimAssist", "Through Walls", ModuleSetting.Type.BOOL, 0, 1,
                AimAssist::isThroughWalls, v -> AimAssist.setThroughWalls((Boolean) v),
                "Aim through walls");
        setting("AimAssist", "Silent Aim", ModuleSetting.Type.BOOL, 0, 1,
                AimAssist::isSilentAim, v -> AimAssist.setSilentAim((Boolean) v),
                "Aim without moving camera");

        // KillAura
        setting("KillAura", "Range", ModuleSetting.Type.DOUBLE, 2.0, 10.0,
                KillAura::getRange, v -> KillAura.setRange((Double) v),
                "Attack range");
        setting("KillAura", "Attack Delay", ModuleSetting.Type.INT, 1, 20,
                KillAura::getAttackDelay, v -> KillAura.setAttackDelay(((Number) v).intValue()),
                "Ticks between attacks");
        setting("KillAura", "Players", ModuleSetting.Type.BOOL, 0, 1,
                KillAura::isPlayers, v -> KillAura.setPlayers((Boolean) v),
                "Target players");
        setting("KillAura", "Animals", ModuleSetting.Type.BOOL, 0, 1,
                KillAura::isAnimals, v -> KillAura.setAnimals((Boolean) v),
                "Target animals");
        setting("KillAura", "Monsters", ModuleSetting.Type.BOOL, 0, 1,
                KillAura::isMonsters, v -> KillAura.setMonsters((Boolean) v),
                "Target monsters");
        setting("KillAura", "Through Walls", ModuleSetting.Type.BOOL, 0, 1,
                KillAura::isThroughWalls, v -> KillAura.setThroughWalls((Boolean) v),
                "Attack through walls");
        setting("KillAura", "Auto Swing", ModuleSetting.Type.BOOL, 0, 1,
                KillAura::isAutoSwing, v -> KillAura.setAutoSwing((Boolean) v),
                "Swing arm on attack");

        // AutoCritical
        setting("AutoCritical", "Only Holding Sword", ModuleSetting.Type.BOOL, 0, 1,
                AutoCritical::isOnlyWhenHoldingSword, v -> AutoCritical.setOnlyWhenHoldingSword((Boolean) v),
                "Only crit while holding a sword");
        setting("AutoCritical", "Silent Mode", ModuleSetting.Type.BOOL, 0, 1,
                AutoCritical::isSilentMode, v -> AutoCritical.setSilentMode((Boolean) v),
                "Crit without visible jump");

        // FastBreak
        setting("FastBreak", "Speed Multiplier", ModuleSetting.Type.DOUBLE, 1.0, 10.0,
                FastBreak::getBreakSpeedMultiplier, v -> FastBreak.setBreakSpeedMultiplier(((Number) v).floatValue()),
                "Block break speed multiplier");
        setting("FastBreak", "Instant Mining", ModuleSetting.Type.BOOL, 0, 1,
                FastBreak::isInstantMining, v -> FastBreak.setInstantMining((Boolean) v),
                "Break blocks instantly");

        // FastPlace
        setting("FastPlace", "Place Delay", ModuleSetting.Type.INT, 1, 10,
                FastPlace::getPlaceDelay, v -> FastPlace.setPlaceDelay(((Number) v).intValue()),
                "Ticks between placements");
        setting("FastPlace", "Instant Place", ModuleSetting.Type.BOOL, 0, 1,
                FastPlace::isInstantPlace, v -> FastPlace.setInstantPlace((Boolean) v),
                "Place blocks instantly");

        // Scaffold
        setting("Scaffold", "Place Under Feet", ModuleSetting.Type.BOOL, 0, 1,
                Scaffold::isPlaceUnderFeet, v -> Scaffold.setPlaceUnderFeet((Boolean) v),
                "Place below the player");
        setting("Scaffold", "Place Ahead", ModuleSetting.Type.BOOL, 0, 1,
                Scaffold::isPlaceAhead, v -> Scaffold.setPlaceAhead((Boolean) v),
                "Place ahead of the player");
        setting("Scaffold", "Rotate To Place", ModuleSetting.Type.BOOL, 0, 1,
                Scaffold::isRotateToPlace, v -> Scaffold.setRotateToPlace((Boolean) v),
                "Rotate toward placement");
        setting("Scaffold", "Tower Mode", ModuleSetting.Type.BOOL, 0, 1,
                Scaffold::isTowerMode, v -> Scaffold.setTowerMode((Boolean) v),
                "Tower upward");
        setting("Scaffold", "Place Delay", ModuleSetting.Type.INT, 1, 10,
                Scaffold::getPlaceDelay, v -> Scaffold.setPlaceDelay(((Number) v).intValue()),
                "Ticks between placements");

        // OreESP
        setting("OreESP", "Radius", ModuleSetting.Type.INT, 4, 32,
                OreESP::getRadius, v -> OreESP.setRadius(((Number) v).intValue()),
                "Ore scan radius");

        // Tracers
        setting("Tracers", "Thickness", ModuleSetting.Type.DOUBLE, 0.5, 5.0,
                Tracers::getThickness, v -> Tracers.setThickness((Double) v),
                "Line thickness in pixels");
        setting("Tracers", "Bottom Origin", ModuleSetting.Type.BOOL, 0, 1,
                Tracers::isBottomOrigin, v -> Tracers.setBottomOrigin((Boolean) v),
                "Lines start at bottom of screen (on) or crosshair (off)");
        setting("Tracers", "Color Preset", ModuleSetting.Type.INT, 0, 7,
                () -> colorPresetIndex(Tracers.getColor()),
                v -> Tracers.setColor(COLOR_PRESETS[Math.max(0, Math.min(7, ((Number) v).intValue()))]),
                "Orange, Red, Green, Blue, Yellow, Cyan, Magenta, White");
    }

    private static int colorPresetIndex(int currentColor) {
        for (int i = 0; i < COLOR_PRESETS.length; i++) {
            if (COLOR_PRESETS[i] == currentColor) return i;
        }
        return 0;
    }

    private static void setting(String module,
                                String name,
                                ModuleSetting.Type type,
                                double min,
                                double max,
                                java.util.function.Supplier<Object> getter,
                                java.util.function.Consumer<Object> setter,
                                String description) {
        List<ModuleSetting> list = SETTINGS.get(module);
        if (list == null) return;
        list.add(new ModuleSetting(module, name, type, min, max, getter, setter, description));
    }

    public static boolean isEnabled(String name) {
        return MODULES.getOrDefault(name, false);
    }

    public static void setEnabled(String name, boolean enabled) {
        if (!MODULES.containsKey(name)) return;
        MODULES.put(name, enabled);

        switch (name) {
            case "Speed" -> Speed.setEnabled(enabled);
            case "Fly" -> { if (enabled) Fly.setEnabled(true); else Fly.disable(); }
            case "AirJump" -> AirJump.setEnabled(enabled);
            case "Jesus" -> Jesus.setEnabled(enabled);
            case "Step" -> Step.setEnabled(enabled);
            case "NoFall" -> NoFall.setEnabled(enabled);

            case "Reach" -> Reach.setEnabled(enabled);
            case "AutoClicker" -> AutoClicker.setEnabled(enabled);
            case "AimAssist" -> AimAssist.setEnabled(enabled);
            case "KillAura" -> KillAura.setEnabled(enabled);
            case "AutoCritical" -> AutoCritical.setEnabled(enabled);

            case "FastBreak" -> FastBreak.setEnabled(enabled);
            case "FastPlace" -> FastPlace.setEnabled(enabled);
            case "Scaffold" -> Scaffold.setEnabled(enabled);

            case "OreESP" -> OreESP.setEnabled(enabled);
            case "PlayerESP" -> PlayerESP.setEnabled(enabled);
            case "FullBright" -> FullBright.setEnabled(enabled);
            case "Tracers" -> Tracers.setEnabled(enabled);
            case "ESP" -> ESP.setEnabled(enabled);
            case "EntityHighlight" -> EntityHighlight.setEnabled(enabled);
            case "CriticalHUD" -> CriticalHUD.setEnabled(enabled);
        }
    }

    public static void toggle(String name) {
        setEnabled(name, !isEnabled(name));
    }

    public static List<String> getModules(String category) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> e : CATEGORIES.entrySet()) {
            if (e.getValue().equalsIgnoreCase(category)) result.add(e.getKey());
        }
        return result;
    }

    public static List<String> getVisibleModules(String category) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> e : CATEGORIES.entrySet()) {
            if (e.getValue().equalsIgnoreCase(category) && !HIDDEN.contains(e.getKey())) {
                result.add(e.getKey());
            }
        }
        return result;
    }

    public static List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        for (String category : CATEGORIES.values()) {
            if (!categories.contains(category)) categories.add(category);
        }
        return categories;
    }

    public static List<String> getEnabledModules() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Boolean> e : MODULES.entrySet()) {
            if (e.getValue()) result.add(e.getKey());
        }
        return result;
    }

    public static String getCategory(String module) {
        return CATEGORIES.getOrDefault(module, "UNKNOWN");
    }

    public static List<ModuleSetting> getSettings(String module) {
        List<ModuleSetting> list = SETTINGS.get(module);
        return list == null ? new ArrayList<>() : list;
    }

    public static boolean hasSettings(String module) {
        List<ModuleSetting> list = SETTINGS.get(module);
        return list != null && !list.isEmpty();
    }

    public static void tick() {
        Speed.tick();
        Fly.tick();
        AirJump.tick();
        Jesus.tick();
        Step.tick();
        NoFall.tick();

        Reach.tick();
        AutoClicker.tick();
        AimAssist.tick();
        KillAura.tick();
        AutoCritical.tick();

        FastBreak.tick();
        FastPlace.tick();
        Scaffold.tick();

        OreESP.tick();
        PlayerESP.tick();
        FullBright.tick();
        Tracers.tick();
        ESP.tick();
        EntityHighlight.tick();
        CriticalHUD.tick();
    }
}