package com.xlays.c00lgui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xlays.c00lgui.modules.ModuleManager;
import com.xlays.c00lgui.modules.ModuleSetting;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class C00lGuiConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "c00lgui.json";

    private C00lGuiConfig() {}

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    public static void save() {
        Path path = configPath();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            JsonObject root = new JsonObject();

            // enabled
            JsonObject enabled = new JsonObject();
            for (String category : ModuleManager.getAllCategories()) {
                for (String module : ModuleManager.getVisibleModules(category)) {
                    enabled.addProperty(module, ModuleManager.isEnabled(module));
                }
            }
            root.add("enabled", enabled);

            // settings
            JsonObject settingsRoot = new JsonObject();
            for (String category : ModuleManager.getAllCategories()) {
                for (String module : ModuleManager.getVisibleModules(category)) {
                    List<ModuleSetting> list = ModuleManager.getSettings(module);
                    if (list.isEmpty()) continue;

                    JsonObject moduleSettings = new JsonObject();
                    for (ModuleSetting s : list) {
                        Object v = s.getValue();
                        if (v instanceof Boolean b) {
                            moduleSettings.addProperty(s.getName(), b);
                        } else if (v instanceof Number n) {
                            moduleSettings.addProperty(s.getName(), n);
                        } else if (v != null) {
                            moduleSettings.addProperty(s.getName(), v.toString());
                        }
                    }
                    settingsRoot.add(module, moduleSettings);
                }
            }
            root.add("settings", settingsRoot);

            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(root, w);
            }

        } catch (IOException e) {
            System.err.println("[c00lgui] Failed to save config: " + e.getMessage());
        }
    }

    public static void load() {
        Path path = configPath();
        if (!Files.exists(path)) return;

        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();

            if (root.has("enabled")) {
                JsonObject enabled = root.getAsJsonObject("enabled");
                for (Map.Entry<String, com.google.gson.JsonElement> e : enabled.entrySet()) {
                    String module = e.getKey();
                    boolean value = e.getValue().getAsBoolean();
                    if (ModuleManager.isEnabled(module) != value) {
                        ModuleManager.setEnabled(module, value);
                    }
                }
            }

            if (root.has("settings")) {
                JsonObject settingsRoot = root.getAsJsonObject("settings");
                for (Map.Entry<String, com.google.gson.JsonElement> moduleEntry : settingsRoot.entrySet()) {
                    String module = moduleEntry.getKey();
                    List<ModuleSetting> list = ModuleManager.getSettings(module);
                    if (list.isEmpty()) continue;

                    JsonObject moduleSettings = moduleEntry.getValue().getAsJsonObject();

                    for (ModuleSetting s : list) {
                        if (!moduleSettings.has(s.getName())) continue;
                        com.google.gson.JsonElement value = moduleSettings.get(s.getName());

                        switch (s.getType()) {
                            case BOOL -> s.setValue(value.getAsBoolean());
                            case INT -> s.setValue(value.getAsInt());
                            case DOUBLE -> s.setValue(value.getAsDouble());
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[c00lgui] Failed to load config: " + e.getMessage());
        }
    }
}