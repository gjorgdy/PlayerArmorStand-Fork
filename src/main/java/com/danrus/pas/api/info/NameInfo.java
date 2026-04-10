package com.danrus.pas.api.info;

import com.danrus.pas.api.data.DataStoreKey;
import com.danrus.pas.api.reg.FeatureRegistry;
import com.danrus.pas.config.PasConfig;
import com.danrus.pas.impl.features.CapeFeature;
import com.danrus.pas.impl.features.OverlayFeature;
import com.danrus.pas.impl.features.SkinProviderFeature;
import com.danrus.pas.impl.features.SlimFeature;
import com.danrus.pas.utils.NIParser;
import com.danrus.pas.utils.Rl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class NameInfo {

    public static Map<String, ResourceLocation> MEMES = Map.of(
            "Данечка Разработчик", Rl.pas("textures/lol/danechka_razrabotchik.png"),
            "Дакимакура", Rl.pas("textures/lol/dakimakura.png"),
            "Гига Крео", Rl.pas("textures/lol/gigakreo.png"),
            "Strange Link", Rl.pas("textures/lol/link.png"),
            "Странная ссылка", Rl.pas("textures/lol/link.png"),
            "Сисюлики", Rl.pas("textures/lol/boobs.png"),
            "Джастик", Rl.pas("textures/lol/justik.png")
    );

    private final Map<Class<? extends RenameFeature>, RenameFeature> features = new LinkedHashMap<>();
    private String base;
    public String legacyParams;
    public ResourceLocation lolmeme = null;

    public NameInfo() { this(""); }
    public NameInfo(String base) {
        this.base = base == null ? "" : base;
        initializeFeatures();
    }

    public NameInfo setLolMeme(ResourceLocation texture) {
        this.lolmeme = texture;
        return this;
    }

    private void initializeFeatures() {
        for (Class<? extends RenameFeature> featureClass : FeatureRegistry.getInstance().getOrderedFeatures()) {
            RenameFeature feature = FeatureRegistry.getInstance().createFeature(featureClass);
            if (feature != null) {
                features.put(featureClass, feature);
            }
        }
    }

    public static NameInfo parse(Component input) {
        if (input != null) {
            return parse(input.getString());
        }
        return new NameInfo();
    }

    public static NameInfo parse(String input) {
        String lower = input.toLowerCase();

        if (PasConfig.getInstance().isShowEasterEggs()) {
            ResourceLocation meme = MEMES.get(lower);
            if (meme != null) {
                return new NameInfo().setLolMeme(meme);
            }
        }
        return NIParser.getInstance().parse(input);
    }

    public String compile() {
        StringBuilder out = new StringBuilder();
        out.append(base == null ? "" : base);

        List<String> featureParts = features.values().stream()
                .map(RenameFeature::compile)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (legacyParams != null && !legacyParams.isEmpty()) {
            featureParts.add(0, legacyParams);
        }

        if (!featureParts.isEmpty()) {
            out.append("|").append(String.join("", featureParts));
        }

        return out.toString();
    }


    // --- API ---

    public boolean isEmpty() { return base == null || base.isEmpty(); }

    @SuppressWarnings("unchecked")
    public <T extends RenameFeature> T getFeature(Class<T> featureClass) {
        return (T) features.get(featureClass);
    }

    public void setName(String newName) { this.base = newName == null ? "" : newName; }
    public String base() { return base; }
    public String legacyParams() { return legacyParams; }

    @Override
    public @NotNull String toString() {
        return "NameInfo[" + this.compile() + "(" + this.hashCode() + ")]";
    }

    @Override
    public int hashCode() {
        return base.hashCode() + getFeature(SkinProviderFeature.class).getProvider().hashCode() + (getFeature(SlimFeature.class).isSlim() ? 1 : 2);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NameInfo other &&
                Objects.equals(this.base, other.base) &&
                Objects.equals(this.getFeature(SkinProviderFeature.class).getProvider(),
                        other.getFeature(SkinProviderFeature.class).getProvider()) &&
                this.getFeature(SlimFeature.class).isSlim() == other.getFeature(SlimFeature.class).isSlim();
    }


    // --- Legacy ---

    @Deprecated
    public String getDesiredProvider() {
        SkinProviderFeature feature = getFeature(SkinProviderFeature.class);
        return feature != null ? feature.getProvider() : "M";
    }

    @Deprecated
    public boolean wantBeSlim() {
        SlimFeature feature = getFeature(SlimFeature.class);
        return feature != null && feature.isSlim();
    }

    @Deprecated
    public void setSlim(boolean slim) {
        SlimFeature feature = getFeature(SlimFeature.class);
        if (feature != null) {
            feature.setSlim(slim);
        }
    }

    @Deprecated
    public boolean wantCape() {
        CapeFeature feature = getFeature(CapeFeature.class);
        return feature != null && feature.isEnabled();
    }

    @Deprecated
    public void setCape(boolean cape) {
        CapeFeature feature = getFeature(CapeFeature.class);
        if (feature != null) {
            feature.setEnabled(cape);
        }
    }

    @Deprecated
    public void setOverlay(String texture) {
        OverlayFeature feature = getFeature(OverlayFeature.class);
        if (feature != null) {
            feature.setTexture(texture);
        }
    }

    @Deprecated
    public void setBlend(int blend) {
        OverlayFeature feature = getFeature(OverlayFeature.class);
        if (feature != null) {
            feature.setBlend(blend);
        }
    }

    @Deprecated
    public int blend() {
        OverlayFeature feature = getFeature(OverlayFeature.class);
        return feature != null ? feature.getBlend() : 100;
    }

    @Deprecated
    public String overlay() {
        OverlayFeature feature = getFeature(OverlayFeature.class);
        return feature != null ? feature.getTexture() : "";
    }

    @Deprecated
    public void setProvider(String provider) {
        SkinProviderFeature feature = getFeature(SkinProviderFeature.class);
        if (feature != null) {
            feature.setProvider(provider);
        }
    }

    @Deprecated
    public String capeProvider() {
        CapeFeature feature = getFeature(CapeFeature.class);
        return feature != null ? feature.getProvider() : "";
    }

}
