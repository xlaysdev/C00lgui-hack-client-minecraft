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
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            JsonObject enabled = new JsonObject();
            JsonObject settingsRoot = new JsonObject();

            for (String category : ModuleManager.getAllCategories()) {
                for (String module : ModuleManager.getModules(category)) {
                    enabled.addProperty(module, ModuleManager.isEnabled(module));
                    List<ModuleSetting> list = ModuleManager.getSettings(module);
                    if (list.isEmpty()) continue;

                    JsonObject moduleSettings = new JsonObject();
                    for (ModuleSetting setting : list) {
                        Object value = setting.getValue();
                        if (value instanceof Boolean b) moduleSettings.addProperty(setting.getName(), b);
                        else if (value instanceof Number n) moduleSettings.addProperty(setting.getName(), n);
                        else if (value != null) moduleSettings.addProperty(setting.getName(), value.toString());
                    }
                    settingsRoot.add(module, moduleSettings);
                }
            }

            root.add("enabled", enabled);
            root.add("settings", settingsRoot);
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            System.err.println("[c00lgui] Failed to save config: " + e.getMessage());
        }
    }

    public static void load() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) return;

        try (Reader reader = Files.newBufferedReader(path)) {
            var parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) return;
            JsonObject root = parsed.getAsJsonObject();

            if (root.has("enabled") && root.get("enabled").isJsonObject()) {
                JsonObject enabled = root.getAsJsonObject("enabled");
                for (Map.Entry<String, com.google.gson.JsonElement> entry : enabled.entrySet()) {
                    if (!entry.getValue().isJsonPrimitive()
                            || !entry.getValue().getAsJsonPrimitive().isBoolean()) continue;
                    ModuleManager.setEnabled(entry.getKey(), entry.getValue().getAsBoolean());
                }
            }

            if (root.has("settings") && root.get("settings").isJsonObject()) {
                JsonObject settingsRoot = root.getAsJsonObject("settings");
                for (Map.Entry<String, com.google.gson.JsonElement> moduleEntry : settingsRoot.entrySet()) {
                    if (!moduleEntry.getValue().isJsonObject()) continue;
                    JsonObject moduleSettings = moduleEntry.getValue().getAsJsonObject();

                    for (ModuleSetting setting : ModuleManager.getSettings(moduleEntry.getKey())) {
                        if (!moduleSettings.has(setting.getName())) continue;
                        var value = moduleSettings.get(setting.getName());
                        if (!value.isJsonPrimitive()) continue;
                        try {
                            switch (setting.getType()) {
                                case BOOL -> setting.setValue(value.getAsBoolean());
                                case INT -> setting.setValue(value.getAsInt());
                                case DOUBLE -> setting.setValue(value.getAsDouble());
                            }
                        } catch (RuntimeException ignored) {
                            System.err.println("[c00lgui] Ignoring invalid setting: "
                                    + moduleEntry.getKey() + "." + setting.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[c00lgui] Failed to load config: " + e.getMessage());
        }
    }
}