package com.mendingz.client.gui;

import com.mendingz.client.module.Module;
import com.mendingz.client.module.Module.Category;
import com.mendingz.client.module.ModuleManager;
import com.mendingz.client.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Single draggable window with tabs. Input is polled through GLFW so the code
 * does not depend on the Screen input method signatures that changed in 1.21.9+.
 */
public class ClickGuiScreen extends Screen {
    private static final int W = 320, H = 360, HEADER = 34, TAB_H = 22, ROW = 24, FOOTER = 22;
    private static final int BG = 0xF00D0D11, ROW_BG = 0xFF14141A, ROW_HOVER = 0xFF1B1B23, SUB_BG = 0xFF101015;
    private static final int ACCENT = 0xFFE5173F, ACCENT_DIM = 0xFF5A0B1C, TEXT = 0xFFEDEDED;
    private static final int DIM = 0xFF8A8A94, OFF = 0xFF3A3A44;

    private static int winX = 40, winY = 30;
    private static Category tab = Category.COMBAT;
    private static final double[] scroll = new double[Category.values().length];

    private final boolean[] prevKeys = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private boolean dragging, prevLeft, prevRight, prevRightShift = true;
    private int dragDX, dragDY;
    private Module binding;
    private Setting.Num activeSlider;
    private int sliderX, sliderW;

    // Per-frame input state.
    private int mx, my, listTop, listBottom;
    private boolean click, rclick, left;

    public ClickGuiScreen() {
        super(Text.literal("Mendingz Client"));
        long win = window();
        for (int k = GLFW.GLFW_KEY_SPACE; k <= GLFW.GLFW_KEY_LAST; k++) prevKeys[k] = down(win, k);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static long window() {
        return MinecraftClient.getInstance().getWindow().getHandle();
    }

    private static boolean down(long win, int key) {
        return GLFW.glfwGetKey(win, key) == GLFW.GLFW_PRESS;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long win = window();
        mx = mouseX;
        my = mouseY;
        left = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean right = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        click = left && !prevLeft;
        rclick = right && !prevRight;
        prevLeft = left;
        prevRight = right;

        // Right Shift closes the menu.
        boolean rs = down(win, GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (rs && !prevRightShift) {
            close();
            return;
        }
        prevRightShift = rs;
        pollKeybind(win);

        // Dragging.
        if (click && mx >= winX && mx < winX + W - 24 && my >= winY && my < winY + HEADER) {
            dragging = true;
            dragDX = mx - winX;
            dragDY = my - winY;
        }
        if (!left) dragging = false;
        if (dragging) {
            winX = Math.max(0, Math.min(width - W, mx - dragDX));
            winY = Math.max(0, Math.min(height - H, my - dragDY));
        }
        int x = winX, y = winY;

        ctx.fill(0, 0, width, height, 0x66000000);
        rect(ctx, x - 1, y - 1, W + 2, H + 2, ACCENT_DIM);
        rect(ctx, x, y, W, H, BG);

        // Header.
        ctx.drawText(textRenderer, "Mendingz", x + 14, y + 13, 0xFFFFFFFF, true);
        ctx.drawText(textRenderer, "Client", x + 14 + textRenderer.getWidth("Mendingz "), y + 13, ACCENT, true);
        boolean overClose = hitRaw(x + W - 24, y + 8, x + W - 8, y + 24);
        ctx.drawText(textRenderer, "x", x + W - 19, y + 13, overClose ? ACCENT : DIM, true);
        if (click && overClose) {
            close();
            return;
        }

        // Tabs.
        Category[] cats = Category.values();
        int tw = (W - 20) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            int tx = x + 10 + i * tw;
            int ty = y + HEADER;
            boolean hover = hitRaw(tx, ty, tx + tw - 2, ty + TAB_H);
            boolean active = cats[i] == tab;
            rect(ctx, tx, ty, tw - 2, TAB_H, active ? ACCENT : hover ? ROW_HOVER : ROW_BG);
            String label = cats[i].label;
            ctx.drawText(textRenderer, label, tx + (tw - 2 - textRenderer.getWidth(label)) / 2, ty + 7,
                active ? 0xFFFFFFFF : DIM, false);
            if (click && hover) tab = cats[i];
        }

        // Scrollable list.
        listTop = y + HEADER + TAB_H + 8;
        listBottom = y + H - FOOTER;
        int viewH = listBottom - listTop;
        double maxScroll = Math.max(0, contentHeight() - viewH);
        int ti = tab.ordinal();
        scroll[ti] = Math.max(0, Math.min(maxScroll, scroll[ti]));

        ctx.enableScissor(x + 8, listTop, x + W - 8, listBottom);
        int cy = listTop - (int) scroll[ti];
        int rx = x + 8, rw = W - 16;
        if (tab == Category.CONFIG) {
            for (Setting s : ModuleManager.CONFIG) cy += drawSetting(ctx, s, rx, cy, rw, 0);
        } else {
            for (Module m : ModuleManager.byCategory(tab)) cy += drawModule(ctx, m, rx, cy, rw);
        }
        ctx.disableScissor();

        // Scroll bar.
        if (maxScroll > 0) {
            int barH = Math.max(16, (int) (viewH * (viewH / (viewH + maxScroll))));
            int barY = listTop + (int) ((viewH - barH) * (scroll[ti] / maxScroll));
            ctx.fill(x + W - 6, barY, x + W - 4, barY + barH, ACCENT);
        }

        // Slider drag.
        if (activeSlider != null) {
            if (left) activeSlider.setFromRatio((mx - sliderX) / (double) sliderW);
            else activeSlider = null;
        }

        // Footer.
        String hint = binding != null ? "Press a key (Backspace clears)" : "Right Shift opens. End = panic.";
        ctx.drawText(textRenderer, hint, x + (W - textRenderer.getWidth(hint)) / 2, y + H - 15, DIM, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scroll[tab.ordinal()] -= vertical * 18;
        return true;
    }

    // ---- Keybind capture ----

    private void pollKeybind(long win) {
        for (int k = GLFW.GLFW_KEY_SPACE; k <= GLFW.GLFW_KEY_LAST; k++) {
            boolean d = down(win, k);
            if (d && !prevKeys[k] && binding != null) {
                if (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) binding.key = -1;
                else if (k != GLFW.GLFW_KEY_RIGHT_SHIFT && k != GLFW.GLFW_KEY_END && k != GLFW.GLFW_KEY_ESCAPE)
                    binding.key = k;
                binding = null;
            }
            prevKeys[k] = d;
        }
    }

    // ---- Layout and drawing ----

    private int settingHeight(Setting s) {
        return s instanceof Setting.Num ? 30 : 22;
    }

    private int contentHeight() {
        int h = 0;
        if (tab == Category.CONFIG) {
            for (Setting s : ModuleManager.CONFIG) h += settingHeight(s);
            return h;
        }
        for (Module m : ModuleManager.byCategory(tab)) {
            h += ROW;
            if (m.expanded) for (Setting s : m.settings) h += settingHeight(s);
        }
        return h;
    }

    private int drawModule(DrawContext ctx, Module m, int x, int y, int w) {
        boolean hover = hit(x, y, x + w, y + ROW);
        rect(ctx, x, y, w, ROW, hover ? ROW_HOVER : ROW_BG);

        int switchRight = x + w - 8;
        String chipText = binding == m ? "..." : "Key: " + (m.key == -1 ? "-" : keyName(m.key));
        int chipW = textRenderer.getWidth(chipText) + 10;
        int chipX2 = switchRight - 26 - 8;
        int chipX1 = chipX2 - chipW;
        boolean onChip = hit(chipX1, y + 4, chipX2, y + ROW - 4);

        if (!m.settings.isEmpty()) {
            ctx.drawText(textRenderer, m.expanded ? "v" : ">", x + 8, y + 8, DIM, false);
        }
        ctx.drawText(textRenderer, m.name, x + 22, y + 8, m.enabled ? TEXT : 0xFFB5B5BE, false);
        rect(ctx, chipX1, y + 4, chipW, ROW - 8, binding == m ? ACCENT_DIM : 0xFF1F1F27);
        ctx.drawText(textRenderer, chipText, chipX1 + 5, y + 8, binding == m ? 0xFFFFFFFF : DIM, false);
        drawSwitch(ctx, switchRight, y + ROW / 2, m.enabled);

        if (click && hover) {
            if (onChip) binding = m;
            else if (hit(x, y, x + 20, y + ROW) && !m.settings.isEmpty()) m.expanded = !m.expanded;
            else m.toggle();
        }
        if (rclick && hover && !m.settings.isEmpty()) m.expanded = !m.expanded;

        int used = ROW;
        if (m.expanded) {
            int subStart = y + ROW;
            int subH = 0;
            for (Setting s : m.settings) subH += settingHeight(s);
            ctx.fill(x, subStart, x + w, subStart + subH, SUB_BG);
            ctx.fill(x, subStart, x + 2, subStart + subH, ACCENT);
            int sy = subStart;
            for (Setting s : m.settings) sy += drawSetting(ctx, s, x, sy, w, 14);
            used += subH;
        }
        return used;
    }

    private int drawSetting(DrawContext ctx, Setting s, int x, int y, int w, int indent) {
        int h = settingHeight(s);
        boolean hover = hit(x, y, x + w, y + h);
        int lx = x + 8 + indent;
        int rightEdge = x + w - 8;

        if (s instanceof Setting.Bool b) {
            if (indent == 0) rect(ctx, x, y + 1, w, h - 2, hover ? ROW_HOVER : ROW_BG);
            ctx.drawText(textRenderer, b.name, lx, y + 7, TEXT, false);
            drawSwitch(ctx, rightEdge, y + h / 2, b.value);
            if (click && hover) b.value = !b.value;
        } else if (s instanceof Setting.Num n) {
            if (indent == 0) rect(ctx, x, y + 1, w, h - 2, hover ? ROW_HOVER : ROW_BG);
            ctx.drawText(textRenderer, n.name, lx, y + 4, TEXT, false);
            String v = n.display();
            ctx.drawText(textRenderer, v, rightEdge - textRenderer.getWidth(v), y + 4, DIM, false);

            int trackX = lx;
            int trackW = rightEdge - lx;
            int ty = y + 19;
            ctx.fill(trackX, ty, trackX + trackW, ty + 4, OFF);
            int fillW = (int) (trackW * n.ratio());
            ctx.fill(trackX, ty, trackX + fillW, ty + 4, ACCENT);
            ctx.fill(trackX + fillW - 2, ty - 2, trackX + fillW + 2, ty + 6, 0xFFFFFFFF);

            if (click && hover) {
                activeSlider = n;
                sliderX = trackX;
                sliderW = trackW;
            }
        }
        return h;
    }

    private void drawSwitch(DrawContext ctx, int rightX, int centerY, boolean on) {
        int l = rightX - 26, t = centerY - 6;
        rect(ctx, l, t, 26, 12, on ? ACCENT : OFF);
        rect(ctx, on ? l + 15 : l + 2, t + 2, 9, 8, 0xFFFFFFFF);
    }

    /** Rectangle with notched corners, cheap rounding. */
    private static void rect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + w, y + h - 1, color);
    }

    /** Hit test restricted to the scrollable list area. */
    private boolean hit(int x1, int y1, int x2, int y2) {
        return my >= listTop && my < listBottom && hitRaw(x1, y1, x2, y2);
    }

    private boolean hitRaw(int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private static String keyName(int k) {
        switch (k) {
            case GLFW.GLFW_KEY_SPACE: return "Space";
            case GLFW.GLFW_KEY_TAB: return "Tab";
            case GLFW.GLFW_KEY_ENTER: return "Enter";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LAlt";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RAlt";
            case GLFW.GLFW_KEY_UP: return "Up";
            case GLFW.GLFW_KEY_DOWN: return "Down";
            case GLFW.GLFW_KEY_LEFT: return "Left";
            case GLFW.GLFW_KEY_RIGHT: return "Right";
            default: break;
        }
        if (k >= GLFW.GLFW_KEY_F1 && k <= GLFW.GLFW_KEY_F12) return "F" + (k - GLFW.GLFW_KEY_F1 + 1);
        String n = GLFW.glfwGetKeyName(k, 0);
        return n != null ? n.toUpperCase() : "K" + k;
    }
}
