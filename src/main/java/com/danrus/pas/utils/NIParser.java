package com.danrus.pas.utils;

import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.api.info.RenameFeature;
import com.danrus.pas.api.reg.FeatureRegistry;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

public class NIParser {

    private final Map<String, NameInfo> cached = new WeakHashMap<>();

    public NameInfo parse(String input) {
        if (cached.containsKey(input)) return cached.get(input);
        if (input == null || input.isEmpty()) return new NameInfo();

        String[] divided = input.split("\\|", 2);
        String name = divided[0].trim();

        if (name.matches(".*[<>:\"/\\\\?*].*")) return new NameInfo();

        NameInfo info = new NameInfo(name);

        if (divided.length > 1) {
            String params = divided[1].trim();

            for (Class<? extends RenameFeature> featureClass : FeatureRegistry.getInstance().getOrderedFeatures()) {
                RenameFeature feature = info.getFeature(featureClass);
                if (feature != null && feature.parse(params)) {
                    String compiled = feature.compile();
                    params = params.replace(compiled, "").trim();
                }
            }

            info.legacyParams = normalizeParams(params);
        }

        cached.put(input, info);
        return info;
    }

    private static String normalizeParams(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String p = raw.replaceAll("\\s+", "").toUpperCase();

        for (Class<? extends RenameFeature> featureClass : FeatureRegistry.getInstance().getOrderedFeatures()) {
            RenameFeature feature = FeatureRegistry.getInstance().createFeature(featureClass);
            if (feature != null) {
                Pattern pattern = feature.getCleanupPattern();
                if (pattern != null) {
                    p = pattern.matcher(p).replaceAll("");
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        boolean[] seen = new boolean[256];
        for (int i = 0; i < p.length(); i++) {
            char ch = p.charAt(i);
            if (ch < 256 && !seen[ch]) {
                seen[ch] = true;
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static final NIParser instance = new NIParser();

    public static NIParser getInstance() {
        return instance;
    }
}
