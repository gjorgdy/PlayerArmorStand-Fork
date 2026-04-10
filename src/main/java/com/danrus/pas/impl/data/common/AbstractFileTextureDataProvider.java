package com.danrus.pas.impl.data.common;

import com.danrus.pas.PlayerArmorStandsClient;
import com.danrus.pas.api.data.DataHolder;
import com.danrus.pas.api.data.DataProvider;
import com.danrus.pas.api.data.DataRepository;
import com.danrus.pas.api.DownloadStatus;
import com.danrus.pas.api.data.DataStoreKey;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.api.reg.InfoTranslators;
import com.danrus.pas.utils.TextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractFileTextureDataProvider<T extends DataHolder> implements DataProvider<T> {

    private final Map<DataStoreKey, T> cache = new HashMap<>();

    @Override
    public T get(NameInfo info) {
        DataStoreKey key = getKey(info);
        if (cache.containsKey(key)){
            return cache.get(key);
        }

        if (!info.getDesiredProvider().equals(getProviderCode())) {
            return null;
        }

        if (!isValidName(info.base())) {
            return null;
        }

        Path filePath = getFilePath(info);
        ResourceLocation textureLocation = InfoTranslators.getInstance().toResourceLocation(getDataHolderClass(), info);

        if (filePath.toFile().exists()) {
            Minecraft.getInstance().execute(() -> {
                TextureUtils.registerTexture(filePath, textureLocation, true);
                getDataManager().store(info, createDataHolder(info, textureLocation));
            });
        }

        T data = createDataHolder(info, textureLocation);
        data.setStatus(DownloadStatus.COMPLETED);
        getDataManager().store(info, data);
        return data;
    }

    @Override
    public T find(NameInfo info) {
        T data = cache.get(getKey(info));
        if (data == null) {
            return get(info);
        }
        return data;
    }

    private boolean isValidName(String name) {
        return name != null && !name.isEmpty() && name.length() <= 16 && name.matches("[a-zA-Z0-9_]+");
    }

    @Override
    public boolean delete(NameInfo info) {
        return true;
    }

    @Override
    public boolean delete(DataStoreKey key) {
        return true;
    }

    @Override
    public HashMap<DataStoreKey, T> getAll() {
        return new HashMap<>();
    }

    @Override
    public void store(NameInfo info, T data) {

    }

    @Override
    public void store(DataStoreKey key, T data) {

    }

    @Override
    public void invalidateData(NameInfo info) {
        cache.remove(info.base());
    }

    private List<Path> getCacheFiles() {
        try {
            return List.of(getCachePath().toFile().listFiles()).stream()
                    .filter(file -> file.isFile() && file.getName().endsWith(".png"))
                    .map(file -> file.toPath())
                    .toList();
        } catch (Exception e) {
            PlayerArmorStandsClient.LOGGER.error("Error listing cache files in " + getCachePath(), e);
            return List.of();
        }
    }

    protected abstract Path getFilePath(NameInfo info);
    protected abstract Path getCachePath();
    protected abstract T createDataHolder(NameInfo info, ResourceLocation texture);
    protected abstract DataRepository<T> getDataManager();
    protected abstract String getProviderCode();
    protected abstract Class<? extends DataHolder> getDataHolderClass();
    protected abstract DataStoreKey getKey(NameInfo info);
}
