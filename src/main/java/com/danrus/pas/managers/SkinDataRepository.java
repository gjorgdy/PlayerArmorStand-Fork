package com.danrus.pas.managers;

import com.danrus.pas.api.data.DataStoreKey;
import com.danrus.pas.api.data.TextureProvidersManager;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.impl.data.common.AbstractDataRepository;
import com.danrus.pas.impl.data.skin.*;
import com.danrus.pas.impl.holder.SkinData;

public class SkinDataRepository extends AbstractDataRepository<SkinData> {
    @Override
    protected void prepareSources() {
        addSource(new ClientLevelSkinData(), 0);
        addSource(new MojangDiskSkinData());
        addSource(new NamemcDiskSkinData());
        addSource(new FileTextureSkinData(), 999);
    }

    @Override
    protected SkinData createData(NameInfo info) {
        return new SkinData(info);
    }

    @Override
    protected TextureProvidersManager getTextureProvidersManager() {
        return PasManager.getInstance().getSkinProviderManager();
    }

    @Override
    protected DataStoreKey getCacheKey(NameInfo info) {
        return DataStoreKey.forSkin(info);
    }
}
