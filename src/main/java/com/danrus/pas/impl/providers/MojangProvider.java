package com.danrus.pas.impl.providers;

import com.danrus.pas.ModExecutor;
import com.danrus.pas.PlayerArmorStandsClient;
import com.danrus.pas.api.DownloadStatus;
import com.danrus.pas.api.data.DataHolder;
import com.danrus.pas.api.data.DataRepository;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.api.data.TextureProvider;
import com.danrus.pas.api.reg.InfoTranslators;
import com.danrus.pas.impl.data.common.AbstractDiskDataProvider;
import com.danrus.pas.impl.features.CapeFeature;
import com.danrus.pas.impl.features.SkinProviderFeature;
import com.danrus.pas.impl.holder.CapeData;
import com.danrus.pas.impl.holder.SkinData;
import com.danrus.pas.managers.OverlayMessageManger;
import com.danrus.pas.managers.PasManager;
import com.danrus.pas.utils.MojangUtils;
import com.danrus.pas.utils.RestHelper;
import com.danrus.pas.utils.SkinDownloader;
import com.danrus.pas.utils.EncodeUtils;
import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MojangProvider implements TextureProvider {

    private static final MojangProvider INSTANCE = new MojangProvider();

    private static final String SESSION_SERVER_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";


    private final Map<String, CompletableFuture<Void>> activeDownloads = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private final String literal = "M";

    private MojangProvider() {}

    public static MojangProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public String getLiteral() {
        return literal;
    }

    @Override
    public void load(NameInfo info, Consumer<String> onComplete) {
        synchronized (activeDownloads) {
            CompletableFuture<Void> existing = activeDownloads.get(info.compile());
            if (existing != null) {
                existing.thenAccept(v -> onComplete.accept(info.base()));
                PlayerArmorStandsClient.LOGGER.info("MojangProvider: Reusing active download for " + info.base());
                return;
            }
        }

        if (!MojangUtils.isNicknameValid(info.base())) {
            OverlayMessageManger.getInstance().showInvalidNameMessage(info.base());
            ModExecutor.execute(() -> invalidateAllData(info));
            onComplete.accept(info.base());
            return;
        }

        initializeDownload(info);
        CompletableFuture<Void> downloadFuture = downloadProfile(info, onComplete);

        synchronized (activeDownloads) {
            activeDownloads.put(info.compile(), downloadFuture);
        }

        downloadFuture.whenComplete((v, throwable) -> {
            synchronized (activeDownloads) {
                activeDownloads.remove(info.compile());
            }
        });
    }

    private void initializeDownload(NameInfo info) {
        PlayerArmorStandsClient.LOGGER.info("MojangProvider: Downloading textures for " + info);
        OverlayMessageManger.getInstance().showDownloadMessage(info.base());

        if (info.getFeature(SkinProviderFeature.class).getProvider().equals(getLiteral())) {
            SkinData skinData = new SkinData(info);
            skinData.setStatus(DownloadStatus.IN_PROGRESS);
            PasManager.getInstance().getSkinDataManager().store(info, skinData);
        }

        if (info.getFeature(CapeFeature.class).getProvider().equals(getLiteral())) {
            CapeData capeData = new CapeData(info);
            capeData.setStatus(DownloadStatus.IN_PROGRESS);
            PasManager.getInstance().getCapeDataManager().store(info, capeData);
        }
    }

    private CompletableFuture<Void> downloadProfile(NameInfo info, Consumer<String> onComplete) {
        return MojangUtils.getUUID(info)
                .thenCompose(this::downloadTexturedProfile)
                .thenCompose(texturedProfile -> processTexturedProfile(texturedProfile, info, onComplete))
                .exceptionally(throwable -> {
                    doFail(info);
                    PlayerArmorStandsClient.LOGGER.error("MojangProvider: Failed to download for " + info, throwable);
                    return null;
                });
    }



    private CompletableFuture<TexturedProfile> downloadTexturedProfile(String uuid) {
        return RestHelper.get(SESSION_SERVER_URL + uuid)
                .thenApply(response -> {
                    Profile profile = gson.fromJson(response, Profile.class);
                    if (!isValidProfile(profile)) {
                        throw new RuntimeException("Invalid profile");
                    }

                    String encodedSkin = EncodeUtils.decodeBase64(profile.properties[0].value);
                    TexturedProfile texturedProfile = gson.fromJson(encodedSkin, TexturedProfile.class);

                    if (!isValidTexturedProfile(texturedProfile)) {
                        throw new RuntimeException("Invalid textured profile");
                    }

                    return texturedProfile;
                });
    }

    private CompletableFuture<Void> processTexturedProfile(TexturedProfile profile, NameInfo info, Consumer<String> onComplete) {
        CompletableFuture<Void> skinFuture = processSkinTexture(profile, info);
        CompletableFuture<Void> capeFuture = processCapeTexture(profile, info);

        return CompletableFuture.allOf(skinFuture, capeFuture)
                .thenRun(() -> {
                    OverlayMessageManger.getInstance().showSuccessMessage(info.base());
                    PlayerArmorStandsClient.LOGGER.info("MojangProvider: Successfully downloaded textures for " + info);
                    onComplete.accept(info.base());
                });
    }

    private CompletableFuture<Void> processSkinTexture(TexturedProfile profile, NameInfo info) {
        return processTexture(
                profile.textures.SKIN,
                info,
                () -> !info.getFeature(SkinProviderFeature.class).getProvider().equals("M"),
                SkinData.class,
                SkinData::new,
                PasManager.getInstance().getSkinDataManager(),
                "processSkinTexture",
                true
        );
    }

    private CompletableFuture<Void> processCapeTexture(TexturedProfile profile, NameInfo info) {
        return processTexture(
                profile.textures.CAPE,
                info,
                () -> !info.getFeature(CapeFeature.class).getProvider().equals("M"),
                CapeData.class,
                CapeData::new,
                PasManager.getInstance().getCapeDataManager(),
                "processCapeTexture",
                false
        );
    }

    private <T extends DataHolder> CompletableFuture<Void> processTexture(
            TexturedProfile.Textures.Texture texture,
            NameInfo info,
            Supplier<Boolean> cancelPredicate,
            Class<T> dataClass,
            Supplier<T> dataFactory,
            DataRepository<T> repository,
            String name,
            boolean remap
    ) {
        if (cancelPredicate.get()) {
            return CompletableFuture.completedFuture(null);
        }
        PlayerArmorStandsClient.LOGGER.info(name + " called for {}", info);
        if (texture == null || texture.url == null) {
            T data = dataFactory.get();
            data.setStatus(DownloadStatus.COMPLETED);
            repository.store(info, data);
            return CompletableFuture.completedFuture(null);
        }

        ResourceLocation capeLocation = InfoTranslators.getInstance()
                .toResourceLocation(dataClass, info);
        String fileName = InfoTranslators.getInstance()
                .toFileName(dataClass, info);
        Path filePath = AbstractDiskDataProvider.CACHE_PATH.resolve(fileName + ".png");

        return SkinDownloader.downloadAndRegister(capeLocation, filePath, texture.url, remap)
                .thenAccept(textureId -> {
                    T data = dataFactory.get();
                    data.setTexture(textureId);
                    data.setStatus(DownloadStatus.COMPLETED);
                    AbstractDiskDataProvider.AGES.touch(fileName + ".png");
                    repository.store(info, data);
                });
    }

    private boolean isValidProfile(Profile profile) {
        return profile != null && profile.id != null &&
                profile.properties != null && profile.properties.length > 0;
    }

    private boolean isValidTexturedProfile(TexturedProfile profile) {
        return profile != null && profile.textures != null;
    }

    private void doFail(NameInfo info) {
        OverlayMessageManger.getInstance().showFailMessage(info.base());

        SkinData skinData = new SkinData(info);
        skinData.setStatus(DownloadStatus.FAILED);
        PasManager.getInstance().getSkinDataManager().store(info, skinData);

        CapeData capeData = new CapeData(info);
        capeData.setStatus(DownloadStatus.FAILED);
        PasManager.getInstance().getCapeDataManager().store(info, capeData);
    }

    private void invalidateAllData(NameInfo info) {
        PasManager.getInstance().getSkinDataManager().invalidateData(info);
        PasManager.getInstance().getCapeDataManager().invalidateData(info);
    }

    // Inner classes

    static class Profile {
        public String id;
        public String name;
        public ProfileProperty[] properties;

        static class ProfileProperty {
            public String name;
            public String value;
        }
    }

    static class TexturedProfile {
        public Textures textures;

        static class Textures {
            public Texture SKIN;
            public Texture CAPE;

            static class Texture {
                public String url;
            }
        }
    }
}
