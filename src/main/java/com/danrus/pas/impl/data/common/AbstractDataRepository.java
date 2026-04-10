package com.danrus.pas.impl.data.common;

import com.danrus.pas.PlayerArmorStandsClient;
import com.danrus.pas.api.*;
import com.danrus.pas.api.data.*;
import com.danrus.pas.api.info.NameInfo;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDataRepository<T extends DataHolder> implements DataRepository<T> {

    private final List<DataProvider<T>> sources = new CopyOnWriteArrayList<>();
    private final Map<DataStoreKey, T> cached = new WeakHashMap<>();
    protected final T DEFAULT;

    public AbstractDataRepository(){
        prepareSources();
        DEFAULT = createData(new NameInfo());
    }

    @Override
    public void addSource(DataProvider<T> source) {
        sources.add(source);
    }

    @Override
        public void addSource(DataProvider<T> source, int priority) {
        if (priority < 0) {
            throw new IllegalArgumentException("Priority cannot be negative");
        }
        if (priority >= sources.size()) {
            sources.add(source);
        } else {
            sources.add(priority, source);
        }
    }

    @Override
    public T getData(NameInfo info) {
        if (info.isEmpty()) return DEFAULT;
        if (cached.get(getCacheKey(info)) != null) {
            return cached.get(getCacheKey(info));
        }
        T data = findData(info);
        if (data == null && !info.isEmpty()) {
            data = createData(info);
            data.setStatus(DownloadStatus.IN_PROGRESS);
            store(info, data);
            getTextureProvidersManager().download(info);
            return data;
        }
        return data;
    }

    @Override
    public T findData(NameInfo info) {
        return getFrom(info, tDataProvider -> tDataProvider.find(info));
    }


    @Nullable
    private T getFrom(NameInfo info, Function<DataProvider<T>, T> getter) {
        T data = createData(info);
        boolean needDownload = true;
        for (DataProvider<T> source : sources) {
            T dataFromSource = needDownload ? getter.apply(source) : null;
            if (dataFromSource != null) {
                needDownload = false;
                data = dataFromSource;
            }
        }
        return needDownload ? null : data;
    }

    @Override
    public void store(NameInfo info, T data) {
        sources.forEach(source -> {
            source.store(info, data);
        });
    }

    @Override
    public void store(DataStoreKey key, T data) {
        sources.forEach(source -> {
            source.store(key, data);
        });
    }

    @Override
    public void invalidateData(NameInfo info) {
        cached.remove(getCacheKey(info));
        sources.forEach(source -> source.invalidateData(info));
    }

    @Override
    @Nullable
    public DataProvider<T> getSource(String key) {
                return sources.stream()
                .filter(source -> source.getName().equals(key))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void delete(NameInfo info) {
        cached.remove(getCacheKey(info));
        sources.forEach(source -> {
            if (source.delete(info)) {
                PlayerArmorStandsClient.LOGGER.info("Deleted data from source: {} for name info: {}", source.getName(), info);
            }
        });
    }

    @Override
    public HashMap<String, DataProvider<T>> getSources() {
        return sources.stream()
                .collect(Collectors.toMap(
                        DataProvider::getName,
                        source -> source,
                        (a, b) -> b,
                        HashMap::new
                ));
    }

    @Override
    public Set<DataStoreKey> keySet() {
        Set<DataStoreKey> storeKeys = new HashSet<>();
        getSources().values().forEach(prvd -> {
            storeKeys.addAll(prvd.getAll().keySet());
        });
        return storeKeys;
    }

    protected abstract void prepareSources();
    protected abstract T createData(NameInfo info);
    protected abstract TextureProvidersManager getTextureProvidersManager();
    protected abstract DataStoreKey getCacheKey(NameInfo info);
}
