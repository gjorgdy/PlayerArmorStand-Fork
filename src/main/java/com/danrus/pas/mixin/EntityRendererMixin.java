package com.danrus.pas.mixin;

import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.config.PasConfig;
import com.danrus.pas.impl.features.DisplayNameFeature;
//? if !forge {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//?}
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if < 1.21.10 {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Unique
    private Component getNameWithoutParams(NameInfo info) {
        DisplayNameFeature feature = info.getFeature(DisplayNameFeature.class);
        if (feature != null && feature.isEnabled()) {
            return Component.literal(feature.getName());
        }
        return Component.literal(info.base());
    }

    //? if !forge {

    //? if <1.21.6 {
    @WrapOperation(
            method = "renderNameTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
    )
    private int pas$renderNameTag(Font instance, Component text, float x, float y, int color, boolean dropShadow, Matrix4f pose, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLightCoords, Operation<Integer> original){
        if (text.getString().contains("|") && PasConfig.getInstance().isHideParamsOnLabel() && PasConfig.getInstance().isEnableMod()){
            Component newText = getNameWithoutParams(NameInfo.parse(text));
            x = (float)(-instance.width(newText)) / 2.0f;
            return original.call(instance, newText, x, y, color, dropShadow, pose, bufferSource, displayMode, backgroundColor, packedLightCoords);
        }
        return original.call(instance, text, x, y, color, dropShadow, pose, bufferSource, displayMode, backgroundColor, packedLightCoords);
    }

    @Redirect(
            method = "renderNameTag",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z")
    )
    private boolean pas$deadmau5Equals(String instance, Object object) {
        instance = NameInfo.parse(instance).base();
        return instance.equals(object);
    }
    //?} else if >=1.21.6 && <1.21.9 {

    /*@Shadow
    @Final
    protected net.minecraft.client.renderer.entity.EntityRenderDispatcher entityRenderDispatcher;

    @Inject(
            method = "renderNameTag",
            at = @At("HEAD"),
            cancellable = true
    )
    private <S extends net.minecraft.client.renderer.entity.state.EntityRenderState> void pas$renderNameTag(S renderState, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (renderState instanceof net.minecraft.client.renderer.entity.state.ArmorStandRenderState) {
            Vec3 vec3 = renderState.nameTagAttachment;
            if (vec3 != null) {
                NameInfo info = NameInfo.parse(((net.minecraft.client.renderer.entity.state.ArmorStandRenderState) renderState).customName);
                boolean bl = !renderState.isDiscrete;
                int i = "deadmau5".equals(info.base()) ? -10 : 0;
                poseStack.pushPose();
                poseStack.translate(vec3.x, vec3.y + (double) 0.5F, vec3.z);
                poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                poseStack.scale(0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = poseStack.last().pose();
                Font font = Minecraft.getInstance().font;
                if (displayName.getString().contains("|") && PasConfig.getInstance().isHideParamsOnLabel() && PasConfig.getInstance().isEnableMod()) {
                    displayName = getNameWithoutParams(info);
                }
                float f = (float) (-font.width(displayName)) / 2.0F;
                int j = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
                font.drawInBatch(displayName, f, (float) i, -2130706433, false, matrix4f, bufferSource, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, packedLight);
                if (bl) {
                    font.drawInBatch(displayName, f, (float) i, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, net.minecraft.client.renderer.LightTexture.lightCoordsWithEmission(packedLight, 2));
                }

                poseStack.popPose();
                ci.cancel();
            }
        }
    }
    *///?} else {

        //?}
    //?}
}
