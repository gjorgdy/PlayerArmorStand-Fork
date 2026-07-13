package com.danrus.pas.compat.armorposer.mixin;

//? if armorposer {

import com.danrus.pas.mixin.accessors.ScreenAccessor;
import com.danrus.pas.render.gui.PasConfiguratorScreen;
import com.danrus.pas.impl.namer.ArmorPoserNamer;
import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandScreen.class)
public class ArmorStandScreenMixin {

    @Unique
    //~ screen_render
    private Button configuratorButton = Button.builder(Component.translatable("pas.buttons.configurator"),
                    button -> Minecraft.getInstance().setScreen(new PasConfiguratorScreen(new ArmorPoserNamer((ArmorStandScreen) (Object) this))))
            .tooltip(Tooltip.create(Component.translatable("pas.buttons.configurator.tooltip")))
            .bounds(10, 10, 150, 20)
            .build();


    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void pas$init(CallbackInfo ci) {
        ArmorStandScreen screen = (ArmorStandScreen) (Object) this;
        configuratorButton.setPosition(screen.width / 2 - configuratorButton.getWidth() / 2, screen.height - 50);
        ((ScreenAccessor) this).pas$addRenderableWidget(configuratorButton);
    }
}
//?}
