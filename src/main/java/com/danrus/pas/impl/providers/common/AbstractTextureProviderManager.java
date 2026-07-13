package com.danrus.pas.impl.providers.common;

import com.danrus.pas.api.data.*;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.managers.PasManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractTextureProviderManager<T extends DataHolder> implements TextureProvidersManager {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getName());

    private boolean initialized = false;
    private PasManager pasManager;

    private final Map<String, List<PrioritizedProvider>> providers = new HashMap<>();
    private final List<String> pendingList = new ArrayList<>();

    public void initialize(PasManager manager) {
        if (!initialized) {
            this.pasManager = manager;
            initialized = true;

            this.prepareProviders();
        }
    }
    @Override
    public void addProvider(TextureProvider provider) {
        addProvider(provider, 0);
    }

    @Override
    public void addProvider(TextureProvider provider, int priority) {
        providers
                .computeIfAbsent(provider.getLiteral(), k -> new ArrayList<>())
                .add(new PrioritizedProvider(provider, priority));

        providers.get(provider.getLiteral())
                .sort(Comparator.comparingInt(PrioritizedProvider::priority).reversed());

        if (pasManager != null && pasManager.getExistingProviders() != null && registerAsExistingProvider()) {
            pasManager.getExistingProviders().add(provider.getLiteral());
        }
    }

    @Override
    public void download(NameInfo info) {
        if (pendingList.contains(info.base())) {
            return;
        }

        if (info.base().isEmpty()) {
            LOGGER.warn(getClass().getSimpleName() +
                    ": Invalid input " + info.base());
            return;
        }

        boolean loaded = false;

        for (char c : getExcludeLiterals().toCharArray()) {
            String literal = String.valueOf(c);
            if (getProvider(info).equals(literal)) {
                if (tryLoadFromProviders(literal, info)) {
                    loaded = true;
                    break;
                }
            }
        }

        if (!loaded && !getExcludeLiterals().contains(getProvider(info))) {
            String literal = getProvider(info);
            if (tryLoadFromProviders(literal, info)) {
                loaded = true;
            }
        }

        if (!loaded) {
            if (tryLoadFromProviders(getDefaultLiteral(), info)) {
                loaded = true;
            }
        }

        if (!loaded) {
            LOGGER.error(getClass().getSimpleName() +
                    ": No provider could load " + info.base() + " with NameInfo: " + info);
            if (pasManager != null) {
                this.getDataManager().invalidateData(info);
            }
        }
    }

    private boolean tryLoadFromProviders(String literal, NameInfo info) {
        return tryLoad(providers.get(literal), info);
    }

    private boolean tryLoad(List<PrioritizedProvider> providerList, NameInfo info) {
        if (providerList == null || providerList.isEmpty()) return false;

        for (PrioritizedProvider prioritized : providerList) {
            try {
                LOGGER.debug("Trying to download from {}", prioritized.provider.getClass().getSimpleName());
                pendingList.add(getOutputString(info));
                prioritized.provider().load(info, pendingList::remove);
                return true;
            } catch (Exception e) {
                LOGGER.error(
                        "Provider {} failed to load {}: {}",
                        prioritized.provider().getClass().getSimpleName(), getOutputString(info), e.getMessage()
                );
            }
        }
        return false;
    }

    protected abstract String getOutputString(NameInfo info);

    protected abstract void prepareProviders();
    protected abstract String getProvider(NameInfo info);
    protected abstract String getName();
    protected abstract String getDefaultLiteral();
    protected abstract String getExcludeLiterals();
    protected abstract DataRepository<T> getDataManager();
    protected abstract boolean registerAsExistingProvider();

    private record PrioritizedProvider(TextureProvider provider, int priority) {}
}
