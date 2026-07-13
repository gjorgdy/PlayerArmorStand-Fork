package com.danrus.pas.utils;


import com.danrus.pas.PlayerArmorStandsClient;
import com.danrus.pas.api.data.DataHolder;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.api.reg.InfoTranslators;
import com.danrus.pas.impl.features.CapeFeature;
import com.danrus.pas.impl.features.OverlayFeature;
import com.danrus.pas.impl.holder.CapeData;
import com.danrus.pas.impl.holder.SkinData;
import com.danrus.pas.managers.OverlayMessageManger;
import com.danrus.pas.managers.PasManager;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TextureUtils {
    private static final HashMap<String, ResourceLocation> overlayTextureCache = new HashMap<>();
    private static final List<String> notExistingOverlays = new ArrayList<>();

    public static CompletableFuture<ResourceLocation> registerTexture(Path path, ResourceLocation identifier, boolean remap){
        NativeImage image = parseImageFile(path);
        if (image != null) {
            return registerTexture(image, identifier, remap);
        } else {
            return CompletableFuture.completedFuture(MissingTextureAtlasSprite.getLocation());
        }
    }

    public static void clearOverlayCacheFor(NameInfo info) {
        clearOverlayCacheFor(info.base());
    }

    public static void clearOverlayCacheFor(String baseName) {
        overlayTextureCache.entrySet().removeIf(entry -> entry.getKey().startsWith(baseName + "_"));
    }

    public static CompletableFuture<ResourceLocation> registerTexture(NativeImage image, ResourceLocation identifier, boolean remap) {
        CompletableFuture<ResourceLocation> future = new CompletableFuture<>();

        try {
            if (remap) {
                image = remapTexture(image);
            }

            NativeImage finalImage = image;
            Minecraft.getInstance().execute(() -> {
                try {
                    //? if <=1.21.4 {
                    DynamicTexture texture = new DynamicTexture(finalImage);
                    //?} else {
                    /*DynamicTexture texture = new DynamicTexture(identifier::toString, finalImage);
                    *///?}
                    Minecraft.getInstance().getTextureManager().register(identifier, texture);

                    future.complete(identifier);
                } catch (Exception e) {
                    future.complete(SkinData.DEFAULT_TEXTURE);
                    PlayerArmorStandsClient.LOGGER.warn("Failed to register texture: {}", identifier, e);
                }
            });

        } catch (Exception e) {
            PlayerArmorStandsClient.LOGGER.warn("Failed to process texture: {}", identifier, e);
            future.complete(SkinData.DEFAULT_TEXTURE);
        }

        return future;
    }

    public static void unregisterTexture(ResourceLocation identifier) {
        Minecraft.getInstance().getTextureManager().release(identifier);

        // FIXME: stoopid method to remove from overlay cache, fuck this
        for (String key : new ArrayList<>(overlayTextureCache.keySet())) {
            if (key.contains(identifier.getPath())) {
                overlayTextureCache.remove(key);
                return;
            }
        }

    }

    public static NativeImage parseImageFile(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return NativeImage.read(inputStream);
        } catch (Exception e) {
            PlayerArmorStandsClient.LOGGER.warn("Failed to read texture from path: {}", path.toFile().getName(), e);
            return null;
        }
    }

    public static NativeImage getNativeImage(ResourceLocation identifier) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
        if (texture instanceof DynamicTexture nativeImageBackedTexture) {
            return nativeImageBackedTexture.getPixels();
        } else if (texture instanceof SimpleTexture resourceTexture) {
            try {
                //? if <=1.21.1 {
                /*return resourceTexture.getTextureImage(Minecraft.getInstance().getResourceManager()).getImage();
                *///?} else {
                return resourceTexture.loadContents(Minecraft.getInstance().getResourceManager()).image();
                //?}
            } catch (Exception e) {
                PlayerArmorStandsClient.LOGGER.error("Failed to load texture image for: {}", identifier, e);
                PlayerArmorStandsClient.LOGGER.error("Unknown texture type: {}", texture.getClass().getName());
            }
        }
        return null;

    }

    public static void SaveImage(ResourceLocation identifier, Path path) {
        NativeImage image = getNativeImage(identifier);
        if (image != null) {
            SaveImage(image, path);
        }
    }

    public static void SaveImage(NativeImage image, Path path) {
        try {
            image.writeToFile(path.toFile());
        } catch (Exception e) {
            PlayerArmorStandsClient.LOGGER.warn("Failed to save texture to path: {}", path.toFile().getName(), e);
        }
    }

    private static NativeImage generateSkinProfile(NativeImage image) {
        NativeImage newImage = new NativeImage(16, 16, true);
        int i = image.getHeight();
        int j = image.getWidth();
        if (j == 64 && (i == 32 || i == 64)) {
            newImage.copyRect(image, 8, 8, 4, 2, 8, 8, false, false);
//            newImage.copyRect();
        }
        return newImage;
    }

    private static NativeImage remapTexture(NativeImage image) {
        int i = image.getHeight();
        int j = image.getWidth();
        if (j == 64 && (i == 32 || i == 64)) {
            boolean bl = i == 32;
            if (bl) {
                NativeImage nativeImage = new NativeImage(64, 64, true);
                nativeImage.copyFrom(image);
                image.close();
                image = nativeImage;
                nativeImage.fillRect(0, 32, 64, 32, 0);
                nativeImage.copyRect(4, 16, 16, 32, 4, 4, true, false);
                nativeImage.copyRect(8, 16, 16, 32, 4, 4, true, false);
                nativeImage.copyRect(0, 20, 24, 32, 4, 12, true, false);
                nativeImage.copyRect(4, 20, 16, 32, 4, 12, true, false);
                nativeImage.copyRect(8, 20, 8, 32, 4, 12, true, false);
                nativeImage.copyRect(12, 20, 16, 32, 4, 12, true, false);
                nativeImage.copyRect(44, 16, -8, 32, 4, 4, true, false);
                nativeImage.copyRect(48, 16, -8, 32, 4, 4, true, false);
                nativeImage.copyRect(40, 20, 0, 32, 4, 12, true, false);
                nativeImage.copyRect(44, 20, -8, 32, 4, 12, true, false);
                nativeImage.copyRect(48, 20, -16, 32, 4, 12, true, false);
                nativeImage.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }
            stripAlpha(image, 0, 0, 32, 16);
            if (bl) {
                stripColor(image, 32, 0, 64, 32);
            }

            stripAlpha(image, 0, 16, 64, 32);
            stripAlpha(image, 16, 48, 48, 64);
            return image;
        } else {
            image.close();
            return null;
        }
    }

    //FIXME: hack, need refactor
    public static ResourceLocation getOverlayedTexture(NameInfo info, Class<? extends DataHolder> holderType) {
        OverlayFeature feature = info.getFeature(OverlayFeature.class);

        if (!feature.isEnabled()) {
            if (holderType == CapeData.class) {
                return PasManager.getInstance().getCapeTexture(info);
            } else if (holderType == SkinData.class) {
                return PasManager.getInstance().getSkinTexture(info);
            }
        }

        if (holderType == CapeData.class) {
            CapeFeature capeFeature = info.getFeature(CapeFeature.class);
            return getOverlayTexture(info, feature.getTexture(), CapeData.class, feature.getBlend());
        } else if (holderType == SkinData.class) {
            return getOverlayTexture(info, feature.getTexture(), SkinData.class, feature.getBlend());
        }

        throw new IllegalArgumentException("Unsupported holder type for overlayed texture: " + holderType);
    }


    public static ResourceLocation getOverlayTexture(NameInfo info, String overlay, Class<? extends DataHolder> type, int blendStrength) {
        ResourceLocation location = InfoTranslators.getInstance().toResourceLocation(type, info);
        String cacheKey = location.getPath();
        if (notExistingOverlays.contains(overlay)) {
            switch (type.getSimpleName()) {
                case "CapeData" -> {
                    return PasManager.getInstance().getCapeTexture(info);
                }
                case "SkinData" -> {
                    return PasManager.getInstance().getSkinTexture(info);
                }
                default -> throw new IllegalArgumentException("Unsupported holder type for overlayed texture: " + type);
            }
        }

        ResourceLocation cachedTexture = overlayTextureCache.get(cacheKey);
        if (cachedTexture != null) {
            return cachedTexture;
        }
        ResourceLocation skinId;
        switch (type.getSimpleName()) {
            case "CapeData" -> skinId = PasManager.getInstance().getCapeTexture(info);
            case "SkinData" -> skinId = PasManager.getInstance().getSkinTexture(info);
            default -> throw new IllegalArgumentException("Unsupported holder type for overlayed texture: " + type);
        }
        System.out.println(skinId.getPath());
        AbstractTexture skinTexture = Minecraft.getInstance().getTextureManager().getTexture(skinId);

        ResourceLocation overlayId = Rl.vanilla("textures/block/" + overlay + ".png");
        AbstractTexture overlayTexture = Minecraft.getInstance().getTextureManager().getTexture(overlayId);

        if (overlayTexture == null) {
            OverlayMessageManger.getInstance().showOverlayNotFoundMessage(overlay);
            notExistingOverlays.add(overlay);
            return skinId;
        }

        try {
            if (skinTexture instanceof DynamicTexture skinResourceTexture && overlayTexture instanceof SimpleTexture overlayResourceTexture) {
                NativeImage skinImage = skinResourceTexture.getPixels();
                NativeImage overlayImage = overlayResourceTexture.loadContents(Minecraft.getInstance().getResourceManager()).image();
                NativeImage finalImage = grayscaleSkinOverMaterial(skinImage, overlayImage, (float) blendStrength / 100f);
                registerTexture(finalImage, location, "SkinData".equals(type.getSimpleName()));
                System.out.println("Overlay texture created for: " + cacheKey + " at: " + location);
                overlayTextureCache.put(cacheKey, location);

                return location;
            }
        } catch (Exception e) {
            PlayerArmorStandsClient.LOGGER.warn("Failed to create overlay texture for: {} with overlay: {}", info, overlay, e);
            OverlayMessageManger.getInstance().showOverlayNotFoundMessage(overlay);
            notExistingOverlays.add(overlay);
        }

        return skinId;
    }

    public static NativeImage grayscaleSkinOverMaterial(NativeImage skin, NativeImage material, float blendStrength) {
        int width = skin.getWidth();
        int height = skin.getHeight();

        NativeImage result = new NativeImage(width, height, true);

        int matWidth = material.getWidth();
        int matHeight = material.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int skinArgb = ModUtils.getPixel(skin, x, y);
                int sa = (skinArgb >> 24) & 0xFF;

                if (sa < 10) {
                    ModUtils.setPixel(result, x, y, 0);
                    continue;
                }

                int sr = (skinArgb >> 16) & 0xFF;
                int sg = (skinArgb >> 8) & 0xFF;
                int sb = skinArgb & 0xFF;

                float brightness = (sr + sg + sb) / (3f * 255f);

                int mx = x % matWidth;
                int my = y % matHeight;
                int matArgb = ModUtils.getPixel(material, mx, my);

                int mr = (matArgb >> 16) & 0xFF;
                int mg = (matArgb >> 8) & 0xFF;
                int mb = matArgb & 0xFF;

                int r = (int)(mr * brightness * blendStrength + sr * (1 - blendStrength));
                int g = (int)(mg * brightness * blendStrength + sg * (1 - blendStrength));
                int b = (int)(mb * brightness * blendStrength + sb * (1 - blendStrength));

                int resultArgb = (sa << 24) | (r << 16) | (g << 8) | b;
                ModUtils.setPixel(result, x, y, resultArgb);
            }
        }

        return result;
    }


    private static void stripColor(NativeImage image, int x1, int y1, int x2, int y2) {
        for(int i = x1; i < x2; ++i) {
            for(int j = y1; j < y2; ++j) {
                int k = ModUtils.getPixel(image, i, j);
                if ((k >> 24 & 255) < 128) {
                    return;
                }
            }
        }

        for(int i = x1; i < x2; ++i) {
            for(int j = y1; j < y2; ++j) {
                ModUtils.setPixel(image, i, j, ModUtils.getPixel(image, i, j) & 16777215);
            }
        }
    }
    private static void stripAlpha(NativeImage image, int x1, int y1, int x2, int y2) {
        for(int i = x1; i < x2; ++i) {
            for(int j = y1; j < y2; ++j) {
                ModUtils.setPixel(image, i, j, ModUtils.getPixel(image, i, j) | -16777216);

            }
        }
    }
}