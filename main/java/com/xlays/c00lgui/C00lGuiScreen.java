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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class C00lGuiScreen extends Screen {

    // =====================================================================
    // MAIN PANEL LAYOUT
    // =====================================================================
    private static final int WIDTH = 560;
    private static final int HEIGHT = 320;

    private static final int HEADER_H = 26;
    private static final int FOOTER_H = 16;
    private static final int PAD = 6;

    private static final int COLUMN_W = 130;
    private static final int COLUMN_GAP = 5;
    private static final int ROW_H = 12;
    private static final int COLUMN_HEADER_H = 14;

    // =====================================================================
    // FLOATING SETTINGS WINDOW
    // =====================================================================
    private static final int FW_WIDTH = 210;
    private static final int FW_HEADER_H = 18;
    private static final int FW_ROW_H = 28;
    private static final int FW_PAD = 6;

    // =====================================================================
    // COLORS
    // =====================================================================
    private static final int BG_DIM      = 0x66000000;
    private static final int PANEL_BG    = 0xF0101014;
    private static final int PANEL_HEAD  = 0xFF18181E;
    private static final int PANEL_LINE  = 0xFF2A2A32;
    private static final int COLUMN_BG   = 0xC0141418;
    private static final int ROW_ON_BG   = 0x30FFFFFF;
    private static final int ROW_HOVER   = 0x18FFFFFF;
    private static final int TEXT_ON     = 0xFFFFFFFF;
    private static final int TEXT_OFF    = 0xFF9A9AA2;
    private static final int TEXT_DIM    = 0xFF6A6A72;
    private static final int ACCENT_OFF  = 0xFF3A3A42;
    private static final int CLOSE_BG    = 0xFF3A1A1A;
    private static final int CLOSE_HOV   = 0xFFCC3333;
    private static final int GEAR_BG     = 0xFF1F1F26;
    private static final int GEAR_HOV    = 0xFF3A3A48;

    // Floating window colors
    private static final int FW_BG       = 0xF51A1A22;
    private static final int FW_HEAD     = 0xFF23232E;
    private static final int FW_LINE     = 0xFF35353F;
    private static final int FW_CTRL_BG  = 0x40000000;
    private static final int FW_CTRL_HOV = 0x40FFFFFF;
    private static final int FW_BTN_BG   = 0xFF2A2A36;

    private static final Map<String, Integer> ACCENTS = new LinkedHashMap<>();
    static {
        ACCENTS.put("COMBAT",   0xFFFF5555);
        ACCENTS.put("MOVEMENT", 0xFF55AAFF);
        ACCENTS.put("PLAYER",   0xFFFFD24A);
        ACCENTS.put("VISUAL",   0xFFBB77FF);
    }

    private static final String[] CATEGORIES = { "COMBAT", "MOVEMENT", "PLAYER", "VISUAL" };

    // =====================================================================
    // STATE
    // =====================================================================
    private int left;
    private int top;

    private boolean dragging = false;
    private int dragX = 0;
    private int dragY = 0;

    private boolean hudPanelOpen = false;

    // Floating settings window state
    private String settingsModule = null;
    private int fwX = 0;
    private int fwY = 0;
    private int fwW = FW_WIDTH;
    private int fwH = 100;
    private boolean fwDragging = false;
    private int fwDragX = 0;
    private int fwDragY = 0;
    private boolean fwPositioned = false;

    public C00lGuiScreen() {
        super(Component.literal("c00lgui client"));
    }

    @Override
    protected void init() {
        super.init();
        left = (this.width - WIDTH) / 2;
        top = (this.height - HEIGHT) / 2;
        fwPositioned = false; // re-center on next open
    }

    // =====================================================================
    // RENDER
    // =====================================================================
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, BG_DIM);
        g.fill(left - 2, top - 2, left + WIDTH + 2, top + HEIGHT + 2, 0x40000000);
        g.fill(left, top, left + WIDTH, top + HEIGHT, PANEL_BG);
        drawBorder(g, left, top, left + WIDTH, top + HEIGHT, PANEL_LINE);

        // Header
        g.fill(left, top, left + WIDTH, top + HEADER_H, PANEL_HEAD);
        g.fill(left, top + HEADER_H - 1, left + WIDTH, top + HEADER_H, PANEL_LINE);

        String title = "c00lgui client";
        g.drawString(this.font, title, left + 10, top + 9, TEXT_ON, false);
        int titleW = this.font.width(title);
        long now = System.currentTimeMillis();
        int underline = java.awt.Color.HSBtoRGB((now % 4000L) / 4000.0F, 0.8F, 1.0F);
        g.fill(left + 10, top + 19, left + 10 + titleW, top + 20, underline);

        renderHeaderButtons(g, mouseX, mouseY);

        int contentTop = top + HEADER_H;
        int contentBottom = top + HEIGHT - FOOTER_H;
        int colStartX = left + PAD;

        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            int colX = colStartX + i * (COLUMN_W + COLUMN_GAP);
            renderColumn(g, cat, colX, contentTop + PAD,
                    COLUMN_W, contentBottom - contentTop - PAD * 2, mouseX, mouseY);
        }

        // Footer
        g.fill(left, top + HEIGHT - FOOTER_H, left + WIDTH, top + HEIGHT, PANEL_HEAD);
        g.fill(left, top + HEIGHT - FOOTER_H, left + WIDTH, top + HEIGHT - FOOTER_H + 1, PANEL_LINE);
        String hint = "Right Shift - Open/Close | Left-click toggle | Right-click settings";
        g.drawString(this.font, hint, left + 10, top + HEIGHT - 11, TEXT_DIM, false);

        if (hudPanelOpen) renderHudOverlay(g);
        if (settingsModule != null) renderSettingsWindow(g, mouseX, mouseY);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderHeaderButtons(GuiGraphics g, int mouseX, int mouseY) {
        int closeX = left + WIDTH - 22;
        int closeY = top + 6;
        boolean closeHover = in(mouseX, mouseY, closeX, closeY, 14, 14);
        g.fill(closeX, closeY, closeX + 14, closeY + 14, closeHover ? CLOSE_HOV : CLOSE_BG);
        g.drawString(this.font, "X", closeX + 4, closeY + 3, 0xFFFFFFFF, false);

        int gearX = closeX - 20;
        boolean gearHover = in(mouseX, mouseY, gearX, closeY, 14, 14);
        g.fill(gearX, closeY, gearX + 14, closeY + 14, gearHover ? GEAR_HOV : GEAR_BG);
        g.drawString(this.font, "*", gearX + 5, closeY + 3, 0xFFDDDDDD, false);
    }

    private void renderColumn(GuiGraphics g, String category, int x, int y, int w, int h,
                              int mouseX, int mouseY) {
        int accent = ACCENTS.getOrDefault(category, 0xFFFFFFFF);

        g.fill(x, y, x + w, y + h, COLUMN_BG);
        drawBorder(g, x, y, x + w, y + h, PANEL_LINE);

        g.fill(x, y, x + w, y + COLUMN_HEADER_H, 0xFF1C1C22);
        g.drawString(this.font, titleCase(category), x + 5, y + 3, accent, false);
        g.fill(x, y + COLUMN_HEADER_H, x + w, y + COLUMN_HEADER_H + 1, accent);

        List<String> modules = ModuleManager.getVisibleModules(category);
        int rowY = y + COLUMN_HEADER_H + 1;
        int rowX = x + 1;
        int rowW = w - 2;
        int maxRows = (h - COLUMN_HEADER_H - 1) / ROW_H;

        for (int i = 0; i < modules.size() && i < maxRows; i++) {
            String module = modules.get(i);
            int ry = rowY + i * ROW_H;
            renderRow(g, module, rowX, ry, rowW, ROW_H, mouseX, mouseY, accent);
        }
    }

    private void renderRow(GuiGraphics g, String module, int x, int y, int w, int h,
                           int mouseX, int mouseY, int accent) {
        boolean enabled = ModuleManager.isEnabled(module);
        boolean hover = in(mouseX, mouseY, x, y, w, h);

        if (hover)          g.fill(x, y, x + w, y + h, ROW_HOVER);
        else if (enabled)   g.fill(x, y, x + w, y + h, ROW_ON_BG);

        int barColor = enabled ? accent : ACCENT_OFF;
        g.fill(x + 2, y + 2, x + 3, y + h - 2, barColor);

        int nameColor = enabled ? TEXT_ON : TEXT_OFF;
        g.drawString(this.font, module, x + 8, y + (h - 8) / 2, nameColor, false);
    }

    // =====================================================================
    // FLOATING SETTINGS WINDOW
    // =====================================================================
    private void renderSettingsWindow(GuiGraphics g, int mouseX, int mouseY) {
        List<ModuleSetting> settings = ModuleManager.getSettings(settingsModule);
        int rows = settings.isEmpty() ? 1 : settings.size();
        fwH = FW_HEADER_H + FW_PAD + rows * FW_ROW_H + FW_PAD;

        // Auto-center on first open
        if (!fwPositioned) {
            fwX = left + (WIDTH - fwW) / 2;
            fwY = top + (HEIGHT - fwH) / 2;
            fwPositioned = true;
        }

        int accent = ACCENTS.getOrDefault(ModuleManager.getCategory(settingsModule), 0xFFFFFFFF);

        // Shadow
        g.fill(fwX - 2, fwY - 2, fwX + fwW + 2, fwY + fwH + 2, 0x50000000);

        // Body
        g.fill(fwX, fwY, fwX + fwW, fwY + fwH, FW_BG);
        drawBorder(g, fwX, fwY, fwX + fwW, fwY + fwH, FW_LINE);

        // Header
        g.fill(fwX, fwY, fwX + fwW, fwY + FW_HEADER_H, FW_HEAD);
        g.fill(fwX, fwY + FW_HEADER_H - 1, fwX + fwW, fwY + FW_HEADER_H, accent);

        String headText = "Settings - " + settingsModule;
        g.drawString(this.font, headText, fwX + 6, fwY + 5, accent, false);

        int closeX = fwX + fwW - 15;
        int closeY = fwY + 3;
        boolean closeHover = in(mouseX, mouseY, closeX, closeY, 12, 12);
        g.fill(closeX, closeY, closeX + 12, closeY + 12, closeHover ? CLOSE_HOV : CLOSE_BG);
        g.drawString(this.font, "X", closeX + 4, closeY + 2, 0xFFFFFFFF, false);

        if (settings.isEmpty()) {
            g.drawString(this.font, "No settings", fwX + 8, fwY + FW_HEADER_H + 8, TEXT_DIM, false);
            return;
        }

        // Settings rows
        int sy = fwY + FW_HEADER_H + FW_PAD;
        int rowX = fwX + 8;
        int rowW = fwW - 16;

        for (ModuleSetting s : settings) {
            renderFloatingSettingRow(g, s, rowX, sy, rowW, mouseX, mouseY);
            sy += FW_ROW_H;
        }
    }

    private void renderFloatingSettingRow(GuiGraphics g, ModuleSetting s, int x, int y, int w,
                                          int mouseX, int mouseY) {
        // Label on top
        g.drawString(this.font, s.getName(), x, y, TEXT_OFF, false);

        int controlY = y + 11;
        switch (s.getType()) {
            case BOOL -> {
                boolean on = s.getValue() instanceof Boolean b && b;
                int bx = x;
                int bw = 30;
                int bh = 12;
                boolean hover = in(mouseX, mouseY, bx, controlY, bw, bh);
                g.fill(bx, controlY, bx + bw, controlY + bh, hover ? FW_CTRL_HOV : FW_CTRL_BG);
                drawBorder(g, bx, controlY, bx + bw, controlY + bh, FW_LINE);
                if (on) g.fill(bx + 1, controlY + 1, bx + bw - 1, controlY + bh - 1, 0xFF55FF88);
                g.drawString(this.font, on ? "ON" : "OFF", bx + bw + 6, controlY + 2,
                        on ? TEXT_ON : TEXT_DIM, false);
            }
            case INT, DOUBLE -> {
                int minusX = x;
                int plusX  = x + w - 20;
                int valueX = minusX + 22;
                int valueW = plusX - valueX - 4;
                int bh = 12;

                boolean minusHover = in(mouseX, mouseY, minusX, controlY, 20, bh);
                boolean plusHover  = in(mouseX, mouseY, plusX, controlY, 20, bh);
                boolean valueHover = in(mouseX, mouseY, valueX, controlY, valueW, bh);

                // Minus
                g.fill(minusX, controlY, minusX + 20, controlY + bh, minusHover ? FW_CTRL_HOV : FW_BTN_BG);
                drawBorder(g, minusX, controlY, minusX + 20, controlY + bh, FW_LINE);
                g.drawString(this.font, "-", minusX + 8, controlY + 2, TEXT_ON, false);

                // Plus
                g.fill(plusX, controlY, plusX + 20, controlY + bh, plusHover ? FW_CTRL_HOV : FW_BTN_BG);
                drawBorder(g, plusX, controlY, plusX + 20, controlY + bh, FW_LINE);
                g.drawString(this.font, "+", plusX + 8, controlY + 2, TEXT_ON, false);

                // Value
                if (valueHover) g.fill(valueX, controlY, valueX + valueW, controlY + bh, 0x15FFFFFF);
                String val = s.displayValue();
                int vw = this.font.width(val);
                g.drawString(this.font, val, valueX + (valueW - vw) / 2, controlY + 2, TEXT_ON, false);
            }
        }
    }

    private void renderHudOverlay(GuiGraphics g) {
        int bw = 220, bh = 100;
        int bx = (this.width - bw) / 2;
        int by = (this.height - bh) / 2;
        g.fill(0, 0, this.width, this.height, 0x88000000);
        g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, PANEL_LINE);
        g.fill(bx, by, bx + bw, by + bh, PANEL_BG);
        g.drawString(this.font, "HUD Settings", bx + 10, by + 8, TEXT_ON, false);
        g.drawString(this.font, "Rainbow HUD always on", bx + 10, by + 28, TEXT_DIM, false);
        g.drawString(this.font, "Click X or ESC to close", bx + 10, by + 46, TEXT_DIM, false);
    }

    // =====================================================================
    // INPUT
    // =====================================================================
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        int x = (int) event.x();
        int y = (int) event.y();

        // HUD overlay takes priority
        if (hudPanelOpen) {
            hudPanelOpen = false;
            return true;
        }

        // Floating settings window takes priority over the main panel
        if (settingsModule != null) {
            // Close button
            int closeX = fwX + fwW - 15;
            int closeY = fwY + 3;
            if (in(x, y, closeX, closeY, 12, 12)) {
                settingsModule = null;
                fwDragging = false;
                C00lGuiConfig.save();
                return true;
            }

            // Header drag
            if (in(x, y, fwX, fwY, fwW, FW_HEADER_H)) {
                fwDragging = true;
                fwDragX = x - fwX;
                fwDragY = y - fwY;
                return true;
            }

            // Setting controls
            if (button == 0) {
                List<ModuleSetting> settings = ModuleManager.getSettings(settingsModule);
                int sy = fwY + FW_HEADER_H + FW_PAD;
                int rowX = fwX + 8;
                int rowW = fwW - 16;

                for (ModuleSetting s : settings) {
                    int controlY = sy + 11;
                    switch (s.getType()) {
                        case BOOL -> {
                            if (in(x, y, rowX, controlY, 30, 12)) {
                                s.toggleBool();
                                C00lGuiConfig.save();
                                return true;
                            }
                        }
                        case INT, DOUBLE -> {
                            int minusX = rowX;
                            int plusX = rowX + rowW - 20;

                            double step = s.getType() == ModuleSetting.Type.INT
                                    ? 1.0
                                    : Math.max(0.01, (s.getMax() - s.getMin()) / 100.0);

                            if (in(x, y, minusX, controlY, 20, 12)) {
                                s.adjust(-step);
                                C00lGuiConfig.save();
                                return true;
                            }
                            if (in(x, y, plusX, controlY, 20, 12)) {
                                s.adjust(step);
                                C00lGuiConfig.save();
                                return true;
                            }
                        }
                    }
                    sy += FW_ROW_H;
                }
            }

            // Clicks anywhere else while window is open: close it (feels natural)
            // UNLESS they hit the main panel — in that case let them through so you
            // can interact with the panel behind. For simplicity here, close it.
            if (!in(x, y, fwX, fwY, fwW, fwH)) {
                // Clicked outside — close the floating window
                settingsModule = null;
                fwDragging = false;
                return true;
            }
            return true;
        }

        // Header buttons (only when no floating window is open)
        if (button == 0) {
            int closeX = left + WIDTH - 22;
            int closeY = top + 6;
            if (in(x, y, closeX, closeY, 14, 14)) {
                C00lGuiConfig.save();
                Minecraft.getInstance().setScreen(null);
                return true;
            }
            int gearX = closeX - 20;
            if (in(x, y, gearX, closeY, 14, 14)) {
                hudPanelOpen = true;
                return true;
            }
            if (in(x, y, left, top, WIDTH, HEADER_H)) {
                dragging = true;
                dragX = x - left;
                dragY = y - top;
                return true;
            }
        }

        // Module columns
        int contentTop = top + HEADER_H;
        int contentBottom = top + HEIGHT - FOOTER_H;
        int colStartX = left + PAD;

        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            int colX = colStartX + i * (COLUMN_W + COLUMN_GAP);
            int colY = contentTop + PAD;
            int colH = contentBottom - contentTop - PAD * 2;
            if (!in(x, y, colX, colY, COLUMN_W, colH)) continue;

            List<String> modules = ModuleManager.getVisibleModules(cat);
            int rowY = colY + COLUMN_HEADER_H + 1;
            int rowX = colX + 1;
            int rowW = COLUMN_W - 2;
            int maxRows = (colH - COLUMN_HEADER_H - 1) / ROW_H;

            for (int m = 0; m < modules.size() && m < maxRows; m++) {
                String module = modules.get(m);
                int ry = rowY + m * ROW_H;
                if (!in(x, y, rowX, ry, rowW, ROW_H)) continue;

                if (button == 1) {
                    // Right-click: open settings window
                    settingsModule = module;
                    fwPositioned = false;
                    return true;
                }

                // Left-click: toggle
                ModuleManager.toggle(module);
                C00lGuiConfig.save();
                return true;
            }
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            dragging = false;
            fwDragging = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (hudPanelOpen && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            hudPanelOpen = false;
            return true;
        }

        if (settingsModule != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            settingsModule = null;
            C00lGuiConfig.save();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            C00lGuiConfig.save();
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (dragging) {
            left = (int) mouseX - dragX;
            top = (int) mouseY - dragY;
            left = Math.max(0, Math.min(this.width - WIDTH, left));
            top = Math.max(0, Math.min(this.height - HEIGHT, top));
        }
        if (fwDragging) {
            fwX = (int) mouseX - fwDragX;
            fwY = (int) mouseY - fwDragY;
            // clamp inside the screen
            fwX = Math.max(0, Math.min(this.width - fwW, fwX));
            fwY = Math.max(0, Math.min(this.height - fwH, fwY));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // =====================================================================
    // HELPERS
    // =====================================================================
    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private static String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}