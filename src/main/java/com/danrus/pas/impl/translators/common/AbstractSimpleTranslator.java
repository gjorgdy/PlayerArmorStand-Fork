package com.danrus.pas.impl.translators.common;

import com.danrus.pas.PlayerArmorStandsClient;
import com.danrus.pas.api.info.InfoTranslator;
import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.utils.Rl;
import com.danrus.pas.utils.EncodeUtils;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractSimpleTranslator implements InfoTranslator {

    @Override
    public boolean isApplicable(NameInfo info) {
        try {

            String provider = getProvider(info);

            if (provider == null) {
                PlayerArmorStandsClient.LOGGER.error("Provider is NULL for {}", info);
                return false;
            }

            boolean result = getLiteral().equals(provider);

            PlayerArmorStandsClient.LOGGER.debug("isApplicable: {} - provider='{}' literal='{}' = {}",
                    this.getClass().getSimpleName(), provider, getLiteral(), result);

            return result;

        } catch (Exception e) {
            PlayerArmorStandsClient.LOGGER.error("Error checking applicability for {}", info, e);
            return false;
        }
    }


    @Override
    public ResourceLocation toResourceLocation(NameInfo info) {
        return Rl.pas(getPrefix() + "/" + toName(info));
    }

    @Override
    public String toFileName(NameInfo info) {
        String name = EncodeUtils.encodeToSha256(getName(info));
        String suffix = getSuffix().isEmpty() ? "" : "_" + getSuffix();
        return name + suffix;
    }

    public String toName(NameInfo info) {
        return EncodeUtils.encodeToSha256(getName(info)) + "_" + info.compile().replaceAll("[^a-z0-9/._-]", "");
    }

    protected abstract String getLiteral();
    protected abstract String getPrefix();
    protected abstract String getSuffix();
    protected abstract String getName(NameInfo info);
    public abstract boolean shouldEncode();
    protected abstract String getProvider(NameInfo info);
}
