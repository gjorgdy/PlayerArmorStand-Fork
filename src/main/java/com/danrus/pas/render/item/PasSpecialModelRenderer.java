package com.danrus.pas.render.item;

import com.danrus.pas.api.data.DataHolder;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.impl.holder.SkinData;
import com.danrus.pas.managers.PasManager;
import com.danrus.pas.render.PasRenderContext;
import com.danrus.pas.render.armorstand.PlayerArmorStandModel;
import com.danrus.pas.utils.Rl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
//? if < 1.21.10 {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class PasSpecialModelRenderer implements SpecialModelRenderer<ItemRenderData> {

    private static ResourceLocation WOOD = Rl.vanilla(
            //? <26.1
            "textures/entity/armorstand/wood.png"
            //? >=26.1
            //"textures/entity/armorstand/armorstand.png"
    );
    private static ResourceLocation STEVE = Rl.vanilla("textures/entity/player/wide/steve.png");

    protected final PlayerArmorStandModel model;
    protected final ArmorStandSpecialRenderer.ArmorStandItemState state;

    protected PasSpecialModelRenderer(PlayerArmorStandModel model, ArmorStandSpecialRenderer.ArmorStandItemState state) {
        this.model = model;
        this.state = state;
    }
    @Override
    //? if <1.21.9 {
    public void render(@Nullable ItemRenderData argument, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        PasRenderContext context = new RenderContext().putData(bufferSource, "bufferSource");
    //?} else if >=1.21.9 <26.1 {
    /*public void submit(@Nullable ItemRenderData argument, ItemDisplayContext displayContext, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoilType, int outlineColor){
        PasRenderContext context = new RenderContext().putData(nodeCollector, "collector").putData(outlineColor, "outlineColor");
    *///?} else {
    /*public void submit(@Nullable ItemRenderData argument, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, int packedLight, int packedOverlay, boolean hasFoilType, int outlineColor) {
        PasRenderContext context = new RenderContext().putData(submitNodeCollector, "collector").putData(outlineColor, "outlineColor");
    *///?}
        NameInfo currentInfo = argument != null ? argument.info() : new NameInfo();
        prepareDraw(argument, poseStack, context, packedLight, packedOverlay, hasFoilType);
        for (ModelPart part : this.model.getOriginalParts()) {
            renderPart(poseStack, part, RenderType.entityCutout(WOOD), context, packedLight, packedOverlay);
        }
        boolean showDefaultSkin = currentInfo.isEmpty() || PlayerArmorStandModel.showArmorStandWhileDownload(argument.dataHolder());
        ResourceLocation location = showDefaultSkin ? STEVE : PasManager.getInstance().getSkinWithOverlayTexture(currentInfo);
        for (ModelPart part : this.model.getPlayerParts()) {
            renderPart(poseStack, part, RenderType.entityTranslucent(location), context, packedLight, packedOverlay);
        }

        if (hasFoilType) {
            for (ModelPart part : this.model.getOriginalParts()) {
                renderPart(poseStack, part, RenderType.glint(), context, packedLight, packedOverlay);
            }

            for (ModelPart part : this.model.getPlayerParts()) {
                renderPart(poseStack, part, RenderType.glint(), context, packedLight, packedOverlay);
            }
        }
    }

    abstract void prepareDraw(ItemRenderData argument, PoseStack poseStack, PasRenderContext context, int packedLight, int packedOverlay, boolean hasFoil);

    private static void renderPart(PoseStack poseStack, ModelPart part,
                                   //? if <1.21.11 {
                                   RenderType
                                   //?} else {
                                   /*net.minecraft.client.renderer.rendertype.RenderType
                                   *///?}
                                           type, PasRenderContext context, int packedLight, int packedOverlay) {
        //? if <1.21.9 {
        MultiBufferSource bufferSource = context.getData(MultiBufferSource.class,"bufferSource");
        VertexConsumer skinConsumer = bufferSource.getBuffer(type);
        part.render(poseStack, skinConsumer, packedLight, packedOverlay);
        //?} else {
        /*net.minecraft.client.renderer.SubmitNodeCollector nodeCollector = context.getData(net.minecraft.client.renderer.SubmitNodeCollector.class,"collector");
        nodeCollector.submitModelPart(part, poseStack, type, packedLight, packedOverlay, null);
        *///?}
    }

    static class RenderContext implements PasRenderContext {

        private final Map<String, Object> contextMap = new HashMap<>(16);

        public <T> PasRenderContext putData(T data, String type) {
            if (contextMap.size() >= 16) {
                throw new IllegalStateException("RenderVersionContext can hold up to 16 data entries.");
            }
            if (data != null) {
                contextMap.put(type, data);
            }
            return this;
        }

        public <T> T getData(Class<T> clazz, String type) {
            try {
                if (!contextMap.containsKey(type)) {
                    throw new IllegalArgumentException("No data found for class: " + clazz.getName());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Error retrieving data for class: " + clazz.getName(), e);
            }
            return clazz.cast(contextMap.get(type));
        }
    }
}
