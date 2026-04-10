package com.danrus.pas.impl.data.common;

import com.danrus.pas.api.*;
import com.danrus.pas.api.data.DataHolder;
import com.danrus.pas.api.data.DataProvider;
import com.danrus.pas.api.data.DataRepository;
import com.danrus.pas.api.data.DataStoreKey;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.config.PasConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class AbstractClientLevelDataProvider<T extends DataHolder> implements DataProvider<T> {

    @Override
    public T get(NameInfo info) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        if (!PasConfig.getInstance().isTryApplyFromServerPlayer()) {
            return null;
        }
        T holder = createDataHolder(info);
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().level.players().stream()
                    .filter(player -> player.getName().getString().equals(info.base()))
                    .findFirst()
                    .ifPresent(
                            player -> {
                                if (getTexture(player) != null) {
                                    holder.setStatus(DownloadStatus.COMPLETED);
                                    holder.setTexture(getTexture(player));
                                }
                            }
                    );
        }



        if (holder.getStatus() == DownloadStatus.COMPLETED) {
            getDataManager().store(info, holder);
            return holder;
        }

        return null;
    }

    @Override
    public T find(NameInfo info) {
        return get(info);
    }

    @Override
    public boolean delete(NameInfo info) {
        return false;
    }

    @Override
    public boolean delete(DataStoreKey key) {
        return false;
    }

    @Override
    public HashMap<DataStoreKey, T> getAll() {
        if (Minecraft.getInstance().level == null) {
            return new HashMap<>();
        }
        return Minecraft.getInstance().level.players().stream()
                .map(player -> {
                    T data = createDataHolder(NameInfo.parse(player.getName().getString()));
                    if (getTexture(player) != null) {
                        data.setStatus(DownloadStatus.COMPLETED);
                        data.setTexture(getTexture(player));
                    }
                    return data;
                })
                // FIXME: wrong key usage, idk how to fix without NameInfo parameter
                .collect(
                        HashMap::new,
                        (map, data) -> map.put( getKey(new NameInfo()) , data),
                        HashMap::putAll
                );
    }

    @Override
    public void store(NameInfo info, T data) {

    }

    @Override
    public void store(DataStoreKey key, T data) {

    }

    @Override
    public void invalidateData(NameInfo info) {

    }

    @Override
    public String getName() {
        return "level";
    }

    @Nullable
    protected abstract ResourceLocation getTexture(AbstractClientPlayer player);
    protected abstract T createDataHolder(NameInfo info);
    protected abstract DataRepository<T> getDataManager();
    protected abstract DataStoreKey getKey(NameInfo info);
}
