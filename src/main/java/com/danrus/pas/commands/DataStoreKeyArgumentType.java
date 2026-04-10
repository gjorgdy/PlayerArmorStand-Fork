package com.danrus.pas.commands;

import com.danrus.pas.api.data.DataHolder;
import com.danrus.pas.api.data.DataRepository;
import com.danrus.pas.api.data.DataStoreKey;
import com.danrus.pas.impl.holder.CapeData;
import com.danrus.pas.impl.holder.SkinData;
import com.danrus.pas.managers.PasManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

public abstract class DataStoreKeyArgumentType<T extends DataHolder> implements ArgumentType<DataStoreKey> {

    public static DataStoreKey getDataStoreKey(CommandContext<?> context, String name) {
        return context.getArgument(name, DataStoreKey.class);
    }

    public static DataStoreKeyArgumentType<SkinData> forSkin() {
        return new DataStoreKeyArgumentType<>() {
            @Override
            DataRepository<SkinData> getDataRepository() {
                return PasManager.getInstance().getSkinDataManager();
            }
        };
    }

    public static DataStoreKeyArgumentType<CapeData> forCape() {
        return new DataStoreKeyArgumentType<>() {
            @Override
            DataRepository<CapeData> getDataRepository() {
                return PasManager.getInstance().getCapeDataManager();
            }
        };
    }

    @Override
    public DataStoreKey parse(StringReader reader) throws CommandSyntaxException {
        DataStoreKey prototype = DataStoreKey.parsePrototype(reader.readString());
        for (DataStoreKey key : getDataRepository().keySet()) {
            if (key.equals(prototype)) {
                return key;
            }
        }
        return prototype;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (DataStoreKey key : getDataRepository().keySet()) {
            builder.suggest(key.asString());
        }
        return builder.buildFuture();
    }

    abstract DataRepository<T> getDataRepository();
}
