package com.mendingz.client;

import com.mendingz.client.gui.ClickGuiScreen;
import com.mendingz.client.gui.MendingzHud;
import com.mendingz.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MendingzClient implements ClientModInitializer {
    private boolean rightShiftDown;
    private boolean endDown;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        HudElementRegistry.attachElementAfter(
            VanillaHudElements.MISC_OVERLAYS,
            Identifier.of("mendingz", "hud"),
            MendingzHud::render
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long window = client.getWindow().getHandle();

            // Right Shift opens the menu (the screen closes itself on the same key).
            boolean rs = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (rs && !rightShiftDown && client.currentScreen == null && client.world != null) {
                client.setScreen(new ClickGuiScreen());
            }
            rightShiftDown = rs;

            // End = panic, disables every module.
            boolean end = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_END) == GLFW.GLFW_PRESS;
            if (end && !endDown) {
                ModuleManager.panic();
            }
            endDown = end;

            ModuleManager.tick(client, window);
        });
    }
}
