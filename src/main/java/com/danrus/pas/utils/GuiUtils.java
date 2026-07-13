package com.danrus.pas.utils;

import com.danrus.pas.mixin.accessors.ScreenAccessor;
import com.danrus.pas.render.gui.PasConfiguratorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class GuiUtils {
    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_WIDTH = 150;

    //~ screen_render
    public static Button.Builder getStandardButtonBuilder(Supplier<PasConfiguratorScreen> screenFactory) {
        return getStandardButtonBuilder(
                button -> {
                    Minecraft.getInstance().setScreen(screenFactory.get());
                }
        );
    }

    public static Button.Builder getStandardButtonBuilder(Button.OnPress onPress) {
        return Button.builder(Component.translatable("pas.buttons.configurator"), onPress)
                .tooltip(Tooltip.create(Component.translatable("pas.buttons.configurator.tooltip")))
                .bounds(10, 10, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public static void configureButtonOnAnvilScreen(Button button, Screen screen) {
        ((ScreenAccessor) screen).pas$addRenderableWidget(button);
        int i = (screen.width - BUTTON_WIDTH) / 2;
        int j = screen.height / 2 + 87;
        button.setPosition(i, j);
    }

    public static void controlButtonActivity(Supplier<Boolean> activate, Supplier<Boolean> disactivate, Button button) {
        if (activate.get()) {
            button.active = true;
            button.visible = true;
        } else if (disactivate.get()) {
            button.active = false;
            button.visible = false;
        }
    }
}
