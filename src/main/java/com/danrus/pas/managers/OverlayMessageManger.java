package com.danrus.pas.managers;

import com.danrus.pas.config.PasConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import net.minecraft.network.chat.Component;

public class OverlayMessageManger {

    private static final OverlayMessageManger INSTANCE = new OverlayMessageManger();

    public void showDownloadMessage(String name) {
        showMessage("pas.downloading", name, ChatFormatting.BLUE);
    }

    public void showInvalidNameMessage(String name) {
        showMessage("pas.invalid_username", name, ChatFormatting.RED);
    }

    public void showSuccessMessage(String name) {
        showMessage("pas.download_success", name, ChatFormatting.GREEN);
    }

    public void showFailMessage(String name) {
        showMessage("pas.download_failed", name, ChatFormatting.RED);
    }

    public void showOverlayNotFoundMessage(String name) {
//        showMessage("pas.overlay_not_found", name, ChatFormatting.RED);
    }

    //~ screen_render
    private void showMessage(String key, String name, ChatFormatting color) {
        if (name.isEmpty()) {
            return;
        }
        switch (PasConfig.getInstance().getDownloadStatusDisplay()) {
            case NONE:
                return;
            case ABOVE_HOTBAR:
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(key, name).withStyle(color), false);
                return;
            case CHAT:
                Minecraft.getInstance().gui.getChat().addMessage(Component.translatable(key, name).withStyle(color));
        }
    }

    public static OverlayMessageManger getInstance() {
        return INSTANCE;
    }
}
