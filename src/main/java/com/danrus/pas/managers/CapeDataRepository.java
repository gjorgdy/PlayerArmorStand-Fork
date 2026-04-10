package com.danrus.pas.managers;

import com.danrus.pas.api.data.DataStoreKey;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.api.data.TextureProvidersManager;
import com.danrus.pas.impl.data.cape.*;
import com.danrus.pas.impl.data.common.AbstractDataRepository;
import com.danrus.pas.impl.holder.CapeData;

public class CapeDataRepository extends AbstractDataRepository<CapeData> {

    @Override
    protected void prepareSources() {
        addSource(new ClientLevelCapeData(), 0);
        addSource(new MojangDiskCapeData());
        addSource(new NamemcDiskCapeData());
        addSource(new MinecraftCapesDiskData());
    }

    @Override
    protected CapeData createData(NameInfo info) {
        return new CapeData(info);
    }

    @Override
    protected TextureProvidersManager getTextureProvidersManager() {
        return PasManager.getInstance().getCapeProviderManager();
    }

    @Override
    protected DataStoreKey getCacheKey(NameInfo info) {
        return DataStoreKey.forCape(info);
    }
}
