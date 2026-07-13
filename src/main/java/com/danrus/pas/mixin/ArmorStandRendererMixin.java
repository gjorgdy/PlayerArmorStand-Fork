package com.danrus.pas.mixin;

import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.config.PasConfig;
import com.danrus.pas.managers.PasManager;
import com.danrus.pas.mixin.accessors.LivingEntityRendererAccessor;
import com.danrus.pas.render.armorstand.ArmorStandCapeLayer;
import com.danrus.pas.render.armorstand.PlayerArmorStandModel;
import com.danrus.pas.utils.ModUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.ArmorStandArmorModel;
//? if < 1.21.10 {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin<S extends LivingEntityRenderState> implements ModUtils.VersionlessArmorStandCape {

    @Unique
    private PlayerArmorStandModel model;

    //? if >= 1.21.4 {
    @Shadow @Mutable @Final
    private ArmorStandArmorModel smallModel;

    @Shadow @Mutable @Final
    private ArmorStandArmorModel bigModel;
    //?}

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void init(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.model = new PlayerArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND));
        ((LivingEntityRendererAccessor) this).invokeAddLayer(new ArmorStandCapeLayer(this));
        ((LivingEntityRendererAccessor) this).setModel(model);
        this.bigModel = model;
        this.smallModel = new PlayerArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL));
    }

    //? if < 1.21.10 {
    @WrapOperation(
            method = "render(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private void pas$renderLol(ArmorStandRenderer instance, S state, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
    //?} else if >=1.21.10 && <26.1 {
    /*@WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    )
    private void pas$renderLol(ArmorStandRenderer instance, S state, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, net.minecraft.client.renderer.state.CameraRenderState cameraRenderState, Operation<Void> original) {
    *///?} else {
    /*@WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    )
    private void pas$renderLol(ArmorStandRenderer instance, LivingEntityRenderState state, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, net.minecraft.client.renderer.state.CameraRenderState cameraRenderState, Operation<Void> original) {
    *///?}
        NameInfo info = NameInfo.parse(ModUtils.getCustomName(state) != null ? ModUtils.getCustomName(state).getString() : "");

        if (info.lolmeme != null) {

            state.bodyRot = 0;
            state.yRot = 0;

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

            Quaternionf rotation = new Quaternionf(dispatcher.camera.rotation());

            rotation = calculateOrientation(dispatcher, rotation);

            poseStack.pushPose();

            poseStack.mulPose(rotation);

            poseStack.translate(0, 1, 0);

            original.call
                    //? if < 1.21.10
                    (instance, state, poseStack, multiBufferSource, i);
                    //? if >= 1.21.10
                    //(instance, state, poseStack, submitNodeCollector, cameraRenderState);

            poseStack.popPose(); // Восстанавливаем состояние
        } else {
            original.call
                    //? if < 1.21.10
                    (instance, state, poseStack, multiBufferSource, i);
                    //? if >= 1.21.10
                    //(instance, state, poseStack, submitNodeCollector, cameraRenderState);
        }
    }

    private Quaternionf calculateOrientation(EntityRenderDispatcher entityRenderDispatcher, Quaternionf quaternion) {
        Camera camera = entityRenderDispatcher.camera;
        return quaternion.rotationYXZ(-0.017453292F * cameraYrot(camera), ((float)Math.PI / 180F) * cameraXRot(camera), 0.0F);
    }

    private static float cameraYrot(Camera camera) {
        //? if < 1.21.11
        return camera.getYRot() - 180.0F;
        //? if >= 1.21.11
        //return camera.yRot() - 180.0F;
    }

    private static float cameraXRot(Camera camera) {
        //? if < 1.21.11
        return -camera.getXRot();
        //? if >= 1.21.11
        //return -camera.xRot();
    }


    @Inject(
            //? if <= 1.21.1 {
            /*method = "getTextureLocation(Lnet/minecraft/world/entity/decoration/ArmorStand;)Lnet/minecraft/resources/ResourceLocation;",
            *///?} else {
            method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;)Lnet/minecraft/resources/ResourceLocation;",
            //?}
            at = @At("RETURN"),
            cancellable = true
    )
    private void textureLocation(
            //? if <= 1.21.1 {
            /*net.minecraft.world.entity.decoration.ArmorStand
            *///?} else {
            net.minecraft.client.renderer.entity.state.ArmorStandRenderState
            //?}
                    armorStand, CallbackInfoReturnable<ResourceLocation> cir){
        if (ModUtils.getCustomName(armorStand) == null || !PasConfig.getInstance().isEnableMod()) {
            return;
        }
        cir.setReturnValue(PasManager.getInstance().getSkinWithOverlayTexture(NameInfo.parse(ModUtils.getCustomName(armorStand))));
    }

    //? if >= 1.21.4 {
    @Inject(
            method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At("HEAD")
    )
    private void pas$setupRotations(net.minecraft.client.renderer.entity.state.ArmorStandRenderState renderState, PoseStack poseStack, float f, float scale, CallbackInfo ci) {
        if (renderState.isUpsideDown && PasConfig.getInstance().isEnableMod() && PasConfig.getInstance().isShowEasterEggs()) {
            poseStack.translate(0.0F, (renderState.boundingBoxHeight + 0.1F) / scale, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
    }
    //?} else {

    /*//? if < 1.21.1 {
    /^@Inject(
            method = "setupRotations(Lnet/minecraft/world/entity/decoration/ArmorStand;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
            at = @At("HEAD")
    )
    private void pas$setupRotations(ArmorStand entityLiving, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci){
    ^///?} else {
    @Inject(
            method = "setupRotations(Lnet/minecraft/world/entity/decoration/ArmorStand;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            at = @At("HEAD")
    )
    private void pas$setupRotations(ArmorStand entityLiving, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci){
    //?}
        if (!PasConfig.getInstance().isEnableMod() || ModUtils.getCustomName(entityLiving) == null) {
            return;
        }

        NameInfo info = NameInfo.parse(entityLiving.getCustomName().getString());

        if (info.base().equalsIgnoreCase("Dinnerbone")
        || info.base().equalsIgnoreCase("Grumm")) {
            poseStack.translate(0.0F, entityLiving.getBbHeight() - 0.1F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
    }

    *///?}

    //? >=1.21.9 {
    /*@Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ArmorStand;Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;F)V",
            at = @At("RETURN")
    )
    private void setCustomName1219(ArmorStand armorStand, net.minecraft.client.renderer.entity.state.ArmorStandRenderState armorStandRenderState, float f, CallbackInfo ci) {
        ((com.danrus.pas.extenders.ArmorStandRenderStateExtender) armorStandRenderState).pas$setCustomName(armorStand.getCustomName());
    }
    *///?}
}
