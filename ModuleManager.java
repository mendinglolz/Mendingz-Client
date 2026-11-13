package com.mendingz.client.module;

import com.mendingz.client.module.Module.Category;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();

    public static final Setting.Bool SHOW_HUD = new Setting.Bool("Show HUD", true);
    public static final Setting.Bool SHOW_MODULE_LIST = new Setting.Bool("Module List", true);
    public static final Setting.Bool NOTIFICATIONS = new Setting.Bool("Notifications", true);
    public static final List<Setting> CONFIG = List.of(SHOW_HUD, SHOW_MODULE_LIST, NOTIFICATIONS);

    private ModuleManager() {}

    private static void add(Category c, String name, int key, Setting... s) {
        MODULES.add(new Module(name, c, key, s));
    }

    public static void init() {
        int none = -1;

        // Combat
        add(Category.COMBAT, "AutoClicker", GLFW.GLFW_KEY_R,
            new Setting.Num("Min CPS", 8, 1, 20, 1), new Setting.Num("Max CPS", 12, 1, 20, 1),
            new Setting.Bool("Hold LMB", false));
        add(Category.COMBAT, "AimAssist", GLFW.GLFW_KEY_G,
            new Setting.Num("Range", 4.5, 1, 6, 0.1), new Setting.Num("FOV", 90, 10, 180, 1),
            new Setting.Num("Speed", 5, 1, 20, 0.5));
        add(Category.COMBAT, "ReachDisplay", none);
        add(Category.COMBAT, "Velocity", GLFW.GLFW_KEY_V,
            new Setting.Num("Horizontal %", 100, 0, 100, 1), new Setting.Num("Vertical %", 100, 0, 100, 1));
        add(Category.COMBAT, "AutoCrystal", GLFW.GLFW_KEY_H, new Setting.Num("Max Self Damage", 6, 0, 20, 1));
        add(Category.COMBAT, "AutoTotem", GLFW.GLFW_KEY_T, new Setting.Num("Health", 6, 1, 20, 1));
        add(Category.COMBAT, "Hitboxes", GLFW.GLFW_KEY_B, new Setting.Num("Expand", 0.1, 0, 0.5, 0.05));
        add(Category.COMBAT, "WTap", none);
        add(Category.COMBAT, "Criticals", none);
        add(Category.COMBAT, "AutoArmor", none);
        add(Category.COMBAT, "AutoPotion", none, new Setting.Num("Health", 8, 1, 20, 1));
        add(Category.COMBAT, "AutoOffhand", none);

        // Movement
        add(Category.MOVEMENT, "Flight", GLFW.GLFW_KEY_F, new Setting.Num("Speed", 1.0, 0.1, 5, 0.1));
        add(Category.MOVEMENT, "Speed", GLFW.GLFW_KEY_K, new Setting.Num("Multiplier", 1.5, 1, 5, 0.1));
        add(Category.MOVEMENT, "ToggleSprint", GLFW.GLFW_KEY_L);
        add(Category.MOVEMENT, "SafeWalk", none);
        add(Category.MOVEMENT, "BridgeAssist", none);
        add(Category.MOVEMENT, "Sneak", GLFW.GLFW_KEY_Z);
        add(Category.MOVEMENT, "FastPlace", none, new Setting.Num("Delay", 0, 0, 4, 1));
        add(Category.MOVEMENT, "AutoTool", none);
        add(Category.MOVEMENT, "AntiAFK", none);
        add(Category.MOVEMENT, "StepAssist", GLFW.GLFW_KEY_N, new Setting.Num("Height", 1.0, 0.5, 2.5, 0.5));

        // Render
        add(Category.RENDER, "PlayerESP", GLFW.GLFW_KEY_E);
        add(Category.RENDER, "Nametags", none);
        add(Category.RENDER, "Fullbright", none);
        add(Category.RENDER, "BlockHighlight", none);
        add(Category.RENDER, "Zoom", GLFW.GLFW_KEY_C, new Setting.Num("Zoom", 4, 1.5, 10, 0.5));
        add(Category.RENDER, "XRay", GLFW.GLFW_KEY_X);
        add(Category.RENDER, "BaseFinder", none,
            new Setting.Bool("Chests", true), new Setting.Bool("Hoppers", true),
            new Setting.Bool("Shulkers", true), new Setting.Bool("Beds", false));
        add(Category.RENDER, "OreFinder", none,
            new Setting.Num("Min Y", -64, -64, 320, 1), new Setting.Num("Max Y", 16, -64, 320, 1),
            new Setting.Bool("Exposed Only", false));
        add(Category.RENDER, "StorageESP", none);
        add(Category.RENDER, "Waypoints", none);
        add(Category.RENDER, "Freecam", none);
        add(Category.RENDER, "ItemESP", none);
        add(Category.RENDER, "Trail", none);

        // Utility
        add(Category.UTILITY, "Friends", none);
        add(Category.UTILITY, "CoordLogger", none);
        add(Category.UTILITY, "AutoReconnect", none);
        add(Category.UTILITY, "SpawnerFinder", none);
    }

    public static List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) if (m.category == c) out.add(m);
        return out;
    }

    public static void panic() {
        for (Module m : MODULES) m.setEnabled(false);
    }

    public static void tick(MinecraftClient mc, long window) {
        for (Module m : MODULES) {
            if (m.key != -1) {
                boolean down = GLFW.glfwGetKey(window, m.key) == GLFW.GLFW_PRESS;
                if (down && !m.keyDown && mc.currentScreen == null && mc.world != null) m.toggle();
                m.keyDown = down;
            }
            if (m.enabled && mc.player != null) m.onTick(mc);
        }
    }
}
