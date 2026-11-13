package com.mendingz.client.gui;

import com.mendingz.client.module.Module;
import com.mendingz.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MendingzHud {
    private static final int ACCENT = 0xFFE5173F;

    private MendingzHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || !ModuleManager.SHOW_HUD.value) return;
        TextRenderer tr = mc.textRenderer;

        // Watermark.
        int w = tr.getWidth("Mendingz Client") + 16;
        ctx.fill(4, 4, 4 + w, 20, 0xB00D0D11);
        ctx.fill(4, 4, 6, 20, ACCENT);
        ctx.drawText(tr, "Mendingz", 10, 8, 0xFFFFFFFF, true);
        ctx.drawText(tr, " Client", 10 + tr.getWidth("Mendingz"), 8, ACCENT, true);

        if (!ModuleManager.SHOW_MODULE_LIST.value) return;

        // Enabled modules, longest name first.
        List<Module> on = new ArrayList<>();
        for (Module m : ModuleManager.MODULES) if (m.enabled) on.add(m);
        on.sort(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed());

        int sw = ctx.getScaledWindowWidth();
        int y = 4;
        for (Module m : on) {
            int tw = tr.getWidth(m.name);
            ctx.fill(sw - tw - 12, y, sw, y + 12, 0xB00D0D11);
            ctx.fill(sw - 3, y, sw, y + 12, ACCENT);
            ctx.drawText(tr, m.name, sw - tw - 8, y + 2, 0xFFFFFFFF, true);
            y += 12;
        }
    }
}
