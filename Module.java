package com.mendingz.client.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public enum Category {
        COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), UTILITY("Utility"), CONFIG("Config");

        public final String label;

        Category(String label) {
            this.label = label;
        }
    }

    public final String name;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public int key; // GLFW key code, -1 = unbound
    public boolean enabled;
    public boolean expanded;
    boolean keyDown;

    public Module(String name, Category category, int key, Setting... settings) {
        this.name = name;
        this.category = category;
        this.key = key;
        this.settings.addAll(List.of(settings));
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    protected void onEnable() {}

    protected void onDisable() {}

    public void onTick(MinecraftClient mc) {}
}
